package com.example.tool.common.model;


import org.springframework.util.Assert;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

public class MyHttpServletRequest implements HttpServletRequest {
    private Map<String, Object> attributes = new HashMap<String, Object>();
    private String servletPath;
    private StringBuffer requestURL;
    private String contextPath;
    private String requestURI;
    private Cookie[] cookies;
    private String characterEncoding;
    private int ContentLength = 0;
    private  long contentLengthLong = 0;
    private  String ContentType;
    Map<String, String[]> parameterMap = new HashMap<>();
    Map<String, List<String>> headMap = new HashMap<>();
    private String remoteAddr;
    private String remoteUser;
    private String remoteHost;
    private int remotePort;
    private String localAddr;
    private String localName;
    private int localPort;
    private String serverName;
    private int serverPort;
    private String method;
    private String protocol;
    private String scheme;
    private String pathInfo;
    private String pathTranslated;
    private String queryString;

    public MyHttpServletRequest(HttpServletRequest request) {
        //复制一遍对象，原来的request失效时会把对象清空
        Enumeration<String> attributeNames = request.getAttributeNames();
        while(attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            this.attributes.put(name, request.getAttribute(name));
        }
        Enumeration<String> parameterNames = request.getParameterNames();
        while(parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            this.parameterMap.put(name, request.getParameterValues(name));
        }
        //复制Cookie
        this.cookies = new Cookie[request.getCookies().length];
        for (int i = 0;i<request.getCookies().length;i++) {
            Cookie cookie = request.getCookies()[i];
            this.cookies[i] = copyCookie(cookie);
        }
        this.servletPath = request.getServletPath();
        this.requestURL = request.getRequestURL();
        this.requestURI = request.getRequestURI();
        this.contextPath = request.getContextPath();
        this.remoteAddr = request.getRemoteAddr();
        this.remoteUser = request.getRemoteUser();
        this.remoteHost = request.getRemoteHost();
        this.remotePort = request.getRemotePort();
        this.localAddr = request.getLocalAddr();
        this.localName = request.getLocalName();
        this.localPort = request.getLocalPort();
        this.characterEncoding = request.getCharacterEncoding();
        this.ContentLength = request.getContentLength();
        this.contentLengthLong = request.getContentLengthLong();
        this.ContentType = request.getContentType();
        Enumeration<String> headerName = request.getHeaderNames();
        while(headerName.hasMoreElements()) {
            String name = headerName.nextElement();
            this.headMap.put(name, Collections.list(request.getHeaders(name)));
        }
        this.serverName = request.getServerName();
        this.serverPort = request.getServerPort();
        this.method = request.getMethod();
        this.protocol = request.getProtocol();
        this.scheme = request.getScheme();
        this.pathInfo = request.getPathInfo();
        this.pathTranslated = request.getPathTranslated();
        this.queryString = request.getQueryString();
    }

    private Enumeration<String> copyEnumeration(Enumeration<String> headerName) {
        Vector<String> copiedVector = new Vector<String>();
        while (headerName.hasMoreElements()) {
            copiedVector.add(headerName.nextElement());
        }
        return copiedVector.elements();
    }
    public static Cookie copyCookie(Cookie oldCookie) {
        Cookie newCookie = new Cookie(oldCookie.getName(), oldCookie.getValue());
        newCookie.setMaxAge(oldCookie.getMaxAge());
        newCookie.setPath(oldCookie.getPath());
        if(oldCookie.getDomain() != null){
            newCookie.setDomain(oldCookie.getDomain());
        }
        newCookie.setSecure(oldCookie.getSecure());
        newCookie.setHttpOnly(oldCookie.isHttpOnly());
        return newCookie;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        // TODO Auto-generated method stub
        return Collections.enumeration(this.attributes.keySet());
    }

    @Override
    public String getCharacterEncoding() {
        // TODO Auto-generated method stub
        return this.characterEncoding;
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        this.characterEncoding = env;

    }

    @Override
    public int getContentLength() {
        return this.ContentLength;
    }

    @Override
    public long getContentLengthLong() {
        return this.contentLengthLong;
    }

    @Override
    public String getContentType() {
        return this.ContentType;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }


    public void addParameter(String name, String... values) {
        Assert.notNull(name, "Parameter name must not be null");
        String[] oldArr = this.parameterMap.get(name);
        if (oldArr != null) {
            String[] newArr = new String[oldArr.length + values.length];
            System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
            System.arraycopy(values, 0, newArr, oldArr.length, values.length);
            this.parameterMap.put(name, newArr);
        }
        else {
            this.parameterMap.put(name, values);
        }
    }

    @Override
    public String getParameter(String name) {
        String[] arr = (name != null ? this.parameterMap.get(name) : null);
        return (arr != null && arr.length > 0 ? arr[0] : null);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        // TODO Auto-generated method stub
        return Collections.enumeration(this.parameterMap.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        // TODO Auto-generated method stub
        return parameterMap.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        // TODO Auto-generated method stub
        return parameterMap;
    }

    @Override
    public String getProtocol() {
        return this.protocol;
    }

    @Override
    public String getScheme() {
        return this.scheme;
    }

    @Override
    public String getServerName() {
        return this.serverName;
    }

    @Override
    public int getServerPort() {
        return this.serverPort;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return this.remoteAddr;
    }

    @Override
    public String getRemoteHost() {
        return this.remoteHost;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isSecure() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRealPath(String path) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getRemotePort() {
        return this.remotePort;
    }

    @Override
    public String getLocalName() {
        return this.localName;
    }

    @Override
    public String getLocalAddr() {
        return this.localAddr;
    }

    @Override
    public int getLocalPort() {
        return this.localPort;
    }

    @Override
    public ServletContext getServletContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
            throws IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAuthType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return cookies;
    }

    @Override
    public long getDateHeader(String name) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getHeader(String name) {
        List<String> list = this.headMap.get(name);
        return list != null && !list.isEmpty()?list.get(0):null;
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(this.headMap.get(name));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(this.headMap.keySet());
    }

    @Override
    public int getIntHeader(String name) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getMethod() {
        return this.method;
    }

    @Override
    public String getPathInfo() {
        return this.pathInfo;
    }

    @Override
    public String getPathTranslated() {
        return this.pathTranslated;
    }

    @Override
    public String getContextPath() {
        return this.contextPath;
    }

    @Override
    public String getQueryString() {
        return this.queryString;
    }

    @Override
    public String getRemoteUser() {
        return this.remoteUser;
    }

    @Override
    public boolean isUserInRole(String role) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRequestURI() {
        return this.requestURI;
    }

    @Override
    public StringBuffer getRequestURL() {
        return this.requestURL;
    }

    @Override
    public String getServletPath() {
        return servletPath;
    }

    @Override
    public HttpSession getSession(boolean create) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HttpSession getSession() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String changeSessionId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {
        // TODO Auto-generated method stub

    }

    @Override
    public void logout() throws ServletException {
        // TODO Auto-generated method stub

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> httpUpgradeHandlerClass)
            throws IOException, ServletException {
        // TODO Auto-generated method stub
        return null;
    }

}