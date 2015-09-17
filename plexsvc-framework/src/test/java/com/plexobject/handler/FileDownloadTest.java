package com.plexobject.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.BasePayload;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.HttpResponse;
import com.plexobject.http.TestWebUtils;
import com.plexobject.service.BaseServiceClient;
import com.plexobject.service.Interceptor;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceRegistry;

public class FileDownloadTest {
    private static ServiceRegistry serviceRegistry;

    @ServiceConfig(protocol = Protocol.HTTP, endpoint = "/files/*", method = RequestMethod.GET, codec = CodecType.SERVICE_SPECIFIC)
    static class FileServer implements RequestHandler {
        private Map<String, String> mimeTypes = new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;
            {
                put(".pdf", "application/pdf");
                put(".js", "application/javascript");
            }
        };
        private static File webFolder = new File("./src/test/resources");

        @Override
        public void handle(Request request) {
            String path = request.getEndpoint().replaceAll("^.files.", "");
            try {
                if (new File(path).isAbsolute()) {
                    throw new IOException("Absolute path '" + path
                            + "' not allowed");
                }
                final String canonicalDirPath = webFolder.getCanonicalPath()
                        + File.separator;
                final File filePath = new File(webFolder, path);

                if (!filePath.getCanonicalPath().startsWith(canonicalDirPath)) {
                    request.getResponse().setContents(
                            new IOException("Relative path '" + path
                                    + "' not allowed"));
                }
                String extension = filePath.getName().substring(
                        filePath.getName().lastIndexOf('.'));
                String contentType = mimeTypes.get(extension);
                if (contentType == null) {
                    contentType = Files.probeContentType(filePath.toPath());
                }
                if (contentType != null) {
                    request.getResponse().setProperty(
                            HttpResponse.CONTENT_TYPE, contentType);
                }
                //
                request.getResponse().setContents(
                        TestWebUtils.toBytes(new FileInputStream(filePath)));
            } catch (IOException e) {
                request.getResponse().setContents(e);
            }
        }
    }

    @BeforeClass
    public static void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty(Constants.HTTP_PORT,
                String.valueOf(BaseServiceClient.DEFAULT_PORT));
        Configuration config = new Configuration(props);
        if (config.getBoolean("logTest")) {
            BasicConfigurator.configure();
            LogManager.getRootLogger().setLevel(Level.INFO);
        }
        serviceRegistry = new ServiceRegistry(config);
        serviceRegistry.addRequestHandler(new FileServer());
        serviceRegistry
                .addInputInterceptor(new Interceptor<BasePayload<String>>() {
                    @Override
                    public BasePayload<String> intercept(
                            BasePayload<String> input) {
                        System.out.println("INPUT\n\tHeaders: "
                                + input.getHeaders() + ", "
                                + input.getProperties() + "\n\tPayload: "
                                + input.getContents() + "\n\n");
                        return input;
                    }
                });
        serviceRegistry
                .addOutputInterceptor(new Interceptor<BasePayload<String>>() {
                    @Override
                    public BasePayload<String> intercept(
                            BasePayload<String> output) {
                        System.out.println("OUTPUT Headers: "
                                + output.getHeaders() + ", "
                                + output.getProperties() + "\n\n");
                        return output;
                    }
                });
        serviceRegistry.start();
        Thread.sleep(500);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        serviceRegistry.stop();
    }

    @Test
    public void testDownloadJavascript() throws Exception {
        byte[] resp = TestWebUtils.getBinary("http://localhost:"
                + BaseServiceClient.DEFAULT_PORT + "/files/test.js");
        File file = new File(FileServer.webFolder, "test.js");
        byte[] expected = TestWebUtils.toBytes(new FileInputStream(file));
        assertTrue(Arrays.equals(expected, resp));
    }

    @Test
    public void testDownloadPDF() throws Exception {
        byte[] resp = TestWebUtils.getBinary("http://localhost:"
                + BaseServiceClient.DEFAULT_PORT + "/files/test.pdf");
        File file = new File(FileServer.webFolder, "test.pdf");
        byte[] expected = TestWebUtils.toBytes(new FileInputStream(file));
        assertEquals("PDF file different", expected.length, resp.length);
        assertTrue(Arrays.equals(expected, resp));
    }
}
