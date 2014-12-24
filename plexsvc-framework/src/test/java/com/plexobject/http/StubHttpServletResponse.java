package com.plexobject.http;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class StubHttpServletResponse implements HttpServletResponse {
    class ServletOutputStreamDelegate extends ServletOutputStream {
        StringBuilder stringBuilder = new StringBuilder();

        @Override
        public void write(int b) throws IOException {
            write(new byte[] { (byte) b }, 0, 1);
        }

        @Override
        public void write(byte b[], int off, int len) throws IOException {
            stringBuilder.append(new String(b, "UTF-8"));
        }

        @Override
        public String toString() {
            return stringBuilder.toString();
        }
    }

    public Properties headers = new Properties();
    public Properties cookies = new Properties();
    public int sc;
    public String msg;
    public String location;
    private ServletOutputStreamDelegate outputStream = new ServletOutputStreamDelegate();

    public String getOutputString() {
        return outputStream.stringBuilder.toString();
    }

    @Override
    public String getCharacterEncoding() {
        return headers.getProperty("Character-Encoding");
    }

    @Override
    public String getContentType() {
        return headers.getProperty("Content-Type");
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(new OutputStreamWriter(outputStream));
    }

    @Override
    public void setCharacterEncoding(String charset) {
        headers.setProperty("Character-Encoding", charset);
    }

    @Override
    public void setContentLength(int len) {
        headers.setProperty("Character-Length", String.valueOf(len));
    }

    @Override
    public void setContentType(String type) {
        headers.setProperty("Character-Type", type);

    }

    @Override
    public void setBufferSize(int size) {
    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {
    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale loc) {
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public void addCookie(Cookie cookie) {
        cookies.setProperty(cookie.getName(), cookie.getValue());
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.contains(name);
    }

    @Override
    public String encodeURL(String url) {
        return url;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return url;
    }

    @Override
    public String encodeUrl(String url) {
        return url;
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return url;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.sc = sc;
        this.msg = msg;
    }

    @Override
    public void sendError(int sc) throws IOException {
        this.sc = sc;
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        this.location = location;
    }

    @Override
    public void setDateHeader(String name, long date) {
        headers.setProperty(name, String.valueOf(date));
    }

    @Override
    public void addDateHeader(String name, long date) {
        headers.setProperty(name, String.valueOf(date));
    }

    @Override
    public void setHeader(String name, String value) {
        headers.setProperty(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        headers.setProperty(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        headers.setProperty(name, String.valueOf(value));
    }

    @Override
    public void addIntHeader(String name, int value) {
        headers.setProperty(name, String.valueOf(value));
    }

    @Override
    public void setStatus(int sc) {
        this.sc = sc;
    }

    @Override
    public void setStatus(int sc, String sm) {
        this.sc = sc;
        this.msg = sm;
    }

}
