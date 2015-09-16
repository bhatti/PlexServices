package com.plexobject.http.servlet;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.plexobject.http.Handledable;
import com.plexobject.http.HttpResponse;
import com.plexobject.http.HttpResponseDispatcher;

public class ServletResponseDispatcher extends HttpResponseDispatcher {
    public ServletResponseDispatcher(final Handledable handledable,
            final HttpServletRequest req, final HttpServletResponse resp) {
        super(handledable, getHttpResponse(req, resp));
    }

    private static HttpResponse getHttpResponse(final HttpServletRequest req,
            final HttpServletResponse resp) {
        return new HttpResponse() {
            private String location;
            private String errorMessage;

            @Override
            public void setContentType(String type) {
                resp.setContentType(type);
            }

            @Override
            public String getContentType() {
                return resp.getContentType();
            }

            @Override
            public void addCookie(String name, String value) {
                resp.addCookie(new Cookie(name, value));
            }

            @Override
            public void sendRedirect(String location) throws IOException {
                this.location = location;
                resp.sendRedirect(location);
            }

            @Override
            public void addHeader(String name, String value) {
                resp.setHeader(name, value);
            }

            @Override
            public void setStatus(int sc) {
                resp.setStatus(sc);
            }

            @Override
            public void send(Object contents) throws IOException {
                if (location != null || errorMessage != null) {
                    return;
                }
                if (contents instanceof String) {
                    resp.getOutputStream()
                            .write(((String) contents).getBytes());
                } else if (contents instanceof byte[]) {
                    resp.getOutputStream().write((byte[]) contents);
                } else {
                    throw new IllegalArgumentException(
                            "Unknown encoded contents " + contents);
                }

                resp.getOutputStream().flush();
            }

            @Override
            public void sendError(int sc, String msg) throws IOException {
                this.errorMessage = msg;
                resp.sendError(sc, msg);
            }
        };
    }
}
