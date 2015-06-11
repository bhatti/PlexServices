package com.plexobject.s3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.HttpResponse;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;
import com.plexobject.service.ServiceConfig;

@ServiceConfig(protocol = Protocol.HTTP, endpoint = "/static/*", method = Method.GET, codec = CodecType.TEXT)
public class StaticFileServer implements RequestHandler {
    private Map<String, String> mimeTypes = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put(".htm", "text/html");
            put(".html", "text/html");
            put(".js", "application/javascript");
        }
    };
    private File webFolder;

    public StaticFileServer(String webdir) throws IOException {
        this.webFolder = new File(webdir);
        if (!webFolder.exists()) {
            throw new FileNotFoundException(webdir + " does not exist");
        }
    }

    @Override
    public void handle(Request request) {
        String path = request.getEndpoint().replaceAll("^.static.", "");
        try {
            if (new File(path).isAbsolute()) {
                throw new IOException("Absolute path '" + path
                        + "' not allowed");
            }
            final String canonicalDirPath = webFolder.getCanonicalPath()
                    + File.separator;
            final File filePath = new File(webFolder, path);

            if (!filePath.getCanonicalPath().startsWith(canonicalDirPath)) {
                request.getResponse().setPayload(
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
                request.getResponse().setProperty(HttpResponse.CONTENT_TYPE,
                        contentType);
            }
            //
            request.getResponse()
                    .setPayload(
                            new String(Files.readAllBytes(Paths.get(filePath
                                    .toURI()))));
        } catch (IOException e) {
            request.getResponse().setPayload(e);
        }
    }
}
