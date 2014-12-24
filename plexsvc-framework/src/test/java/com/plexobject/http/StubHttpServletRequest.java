package com.plexobject.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class StubHttpServletRequest implements HttpServletRequest {
    static class DelegatingServletInputStream extends ServletInputStream {
        private final InputStream sourceStream;

        DelegatingServletInputStream(InputStream sourceStream) {
            this.sourceStream = sourceStream;
        }

        public final InputStream getSourceStream() {
            return this.sourceStream;
        }

        public int read() throws IOException {
            return this.sourceStream.read();
        }

        public void close() throws IOException {
            super.close();
            this.sourceStream.close();
        }

    }

    public String method = "GET";
    public String query;
    public String protocol = "http";
    public String scheme = "http://";
    public Properties attributes = new Properties();
    public Properties parameters = new Properties();
    public Properties headers = new Properties();
    public Properties cookies = new Properties();
    private final ServletInputStream inputStream;
    private final String path;

    public StubHttpServletRequest(String path, String input) {
        this.path = path;
        inputStream = new DelegatingServletInputStream(
                new ByteArrayInputStream(input.getBytes()));
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getAttributeNames() {
        return attributes.propertyNames();
    }

    @Override
    public String getCharacterEncoding() {
        return headers.getProperty("Character-Encoding");
    }

    @Override
    public void setCharacterEncoding(String env)
            throws UnsupportedEncodingException {
        headers.setProperty("Character-Encoding", env);

    }

    @Override
    public int getContentLength() {
        return Integer.parseInt(headers.getProperty("Content-Length", "0"));
    }

    @Override
    public String getContentType() {
        return headers.getProperty("Content-Type");
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return inputStream;
    }

    @Override
    public String getParameter(String name) {
        return parameters.getProperty(name);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getParameterNames() {
        return parameters.propertyNames();
    }

    @Override
    public String[] getParameterValues(String name) {
        return new String[] { parameters.getProperty(name) };
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map getParameterMap() {
        return parameters;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getScheme() {
        // TODO Auto-generated method stub
        return scheme;
    }

    @Override
    public String getServerName() {
        return "localhost";
    }

    @Override
    public int getServerPort() {
        return "http".equals(protocol) ? 80 : 443;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(inputStream));
    }

    @Override
    public String getRemoteAddr() {
        return "127.0.0.1";
    }

    @Override
    public String getRemoteHost() {
        return "localhost";
    }

    @Override
    public void setAttribute(String name, Object o) {
        attributes.put(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);

    }

    @Override
    public Locale getLocale() {
        return Locale.US;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getLocales() {
        return new Enumeration() {
            int i = 0;

            @Override
            public boolean hasMoreElements() {
                return i == 0;
            }

            @Override
            public Object nextElement() {
                i++;
                return getLocale();
            }
        };
    }

    @Override
    public boolean isSecure() {
        return "https".equals(protocol);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public String getRealPath(String path) {
        return path;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return "localhost";
    }

    @Override
    public String getLocalAddr() {
        return "localhost";
    }

    @Override
    public int getLocalPort() {
        return getServerPort();
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        Cookie[] result = new Cookie[cookies.size()];
        int i = 0;
        for (Map.Entry<Object, Object> e : cookies.entrySet()) {
            result[i] = new Cookie(e.getKey().toString(), e.getValue()
                    .toString());
        }
        return result;
    }

    @Override
    public long getDateHeader(String name) {
        return Long.parseLong(headers.getProperty(name, "0"));
    }

    @Override
    public String getHeader(String name) {
        return headers.getProperty(name);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getHeaders(String name) {
        return headers.elements();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getHeaderNames() {
        return headers.propertyNames();
    }

    @Override
    public int getIntHeader(String name) {
        return Integer.parseInt(headers.getProperty(name, "0"));
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getPathInfo() {
        return path;
    }

    @Override
    public String getPathTranslated() {
        return path;
    }

    @Override
    public String getContextPath() {
        return "";
    }

    @Override
    public String getQueryString() {
        return query;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return path;
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    @Override
    public String getServletPath() {
        return path;
    }

    @Override
    public HttpSession getSession(boolean create) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

}
