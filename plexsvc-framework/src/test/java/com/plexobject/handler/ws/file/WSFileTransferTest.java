package com.plexobject.handler.ws.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import javax.jws.WebService;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

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
import com.plexobject.handler.ws.WSRequestHandlerAdapter;
import com.plexobject.http.HttpResponse;
import com.plexobject.http.TestWebUtils;
import com.plexobject.service.BaseServiceClient;
import com.plexobject.service.Interceptor;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;

public class WSFileTransferTest {
    private static ServiceRegistry serviceRegistry;

    @WebService
    public interface TransferService {
        void getFile(String name, Request request);
    }

    @WebService
    @Path("/transferService")
    public static class TransferServiceImpl implements TransferService {
        private static File webFolder = new File("./src/test/resources");

        @Override
        @GET
        public void getFile(@FormParam("name") String name, Request request) {
            try {
                final String canonicalDirPath = webFolder.getCanonicalPath()
                        + File.separator;
                final File filePath = new File(webFolder, name);

                if (!filePath.getCanonicalPath().startsWith(canonicalDirPath)) {
                    request.getResponse().setContents(
                            new IOException("Relative path '" + name
                                    + "' not allowed"));
                }
                //
                byte[] contents = TestWebUtils.toBytes(new FileInputStream(
                        filePath));
                request.getResponse().setCodecType(CodecType.SERVICE_SPECIFIC);
                request.getResponse().setContents(contents);
                request.getResponse().setHeader(HttpResponse.CONTENT_TYPE,
                        "application/pdf");
                request.getResponse().setHeader(HttpResponse.CONTENT_LENGTH,
                        contents.length);
            } catch (IOException e) {
                e.printStackTrace();
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
        WSRequestHandlerAdapter requestHandlerAdapter = new WSRequestHandlerAdapter(
                serviceRegistry);
        Map<ServiceConfigDesc, RequestHandler> handlers = requestHandlerAdapter
                .createFromPackages("com.plexobject.handler.ws.file");
        for (Map.Entry<ServiceConfigDesc, RequestHandler> e : handlers
                .entrySet()) {
            serviceRegistry.addRequestHandler(e.getKey(), e.getValue());
        }
        //
        serviceRegistry
                .addInputInterceptor(new Interceptor<BasePayload<Object>>() {
                    @Override
                    public BasePayload<Object> intercept(
                            BasePayload<Object> input) {
                        System.out.println("INPUT\n\tHeaders: "
                                + input.getHeaders() + ", "
                                + input.getProperties() + "\n\tPayload: "
                                + input.getContents() + "\n\n");
                        return input;
                    }
                });
        serviceRegistry
                .addOutputInterceptor(new Interceptor<BasePayload<Object>>() {
                    @Override
                    public BasePayload<Object> intercept(
                            BasePayload<Object> output) {
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
    public void testDownloadPDF() throws Exception {
        byte[] resp = TestWebUtils.getBinary("http://localhost:"
                + BaseServiceClient.DEFAULT_PORT
                + "/transferService?name=test.pdf");
        File file = new File(TransferServiceImpl.webFolder, "test.pdf");
        byte[] expected = TestWebUtils.toBytes(new FileInputStream(file));
        assertEquals("PDF file different", expected.length, resp.length);
        assertTrue(Arrays.equals(expected, resp));
    }
}
