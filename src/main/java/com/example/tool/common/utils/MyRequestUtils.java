package com.example.tool.common.utils;

import com.example.tool.common.model.MyHttpServletRequest;
import com.example.tool.common.model.MyHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class MyRequestUtils {
	public static ThreadLocal<HttpServletRequest> requestThread = new ThreadLocal<>();
    public static Map<String, Object> readCookie(HttpServletRequest request) {
        Map<String, Object> map=new HashMap<String, Object>();
        Cookie[] cookies = request.getCookies();
        if(cookies!=null&&cookies.length>0) {
            for(Cookie c:cookies) {
                map.put(c.getName(), c.getValue());
            }
        }
        return map;
    }
    public static Object getRequestCookie(String name) {
    	HttpServletRequest request = getRequest();
    	return getRequestCookie(request, name);
    }
    public static Object getRequestCookie(HttpServletRequest request, String name) {
    	Cookie[] cookies = request.getCookies();
        if(cookies!=null&&cookies.length>0) {
            for(Cookie c:cookies) {
            	if(c.getName().equals(name))
            		return c.getValue();
            }
        }
        return null;
    }
    public static Map<String, Object> getRequestCookie() {
    	return readCookie(getRequest());
    }
	public static void setRequest(HttpServletRequest request, HttpServletResponse respons){
		ServletRequestAttributes attributes = new ServletRequestAttributes(request, new MyHttpServletResponse());
		RequestContextHolder.setRequestAttributes(attributes);
	}
	public static void resetRequest(){
		RequestContextHolder.resetRequestAttributes();
	}
    public static HttpServletRequest getRequest(){
		HttpServletRequest request = requestThread.get();
		if(request != null) {
			return request;
		}
		if(RequestContextHolder.getRequestAttributes() != null ) {
			request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
			return request;
		}
		return request;
     }


	public static void setRequestThread(HttpServletRequest request){
		requestThread.set(request);
	}
	public static void setRequestThread(){
		if(RequestContextHolder.getRequestAttributes() != null ) {
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
			requestThread.set(new MyHttpServletRequest(request));
		}
	}
	public static void removeRequestThread(){
		requestThread.remove();
	}

    public static Object getRequestAttribute(String name) {
    	return getRequest().getAttribute(name);
    }
    public static String getRequestAttributeStr(String name) {
    	return (String)getRequest().getAttribute(name);
    }
    public static Object getRequestParameter(String name) {
    	return getRequest().getParameter(name);
    }
    public static String getRequestParameterStr(String name) {
    	HttpServletRequest request = getRequest();
    	return (String)request.getParameter(name);
    }
    public static String getParameterString() {
    	HttpServletRequest httpRequest = getRequest();
    	Enumeration paramNames = httpRequest.getParameterNames();
    	StringBuilder logger = new StringBuilder("\r");
	  	while (paramNames.hasMoreElements()) {
	  	   String paramName = (String) paramNames.nextElement();
	  	   String[] paramValues = httpRequest.getParameterValues(paramName);
	  	   if (paramValues.length == 1) {
		  	    String paramValue = paramValues[0];
		  	    if (paramValue.length() != 0) {
		  	    	logger.append("参数：" + paramName + "=" + paramValue+"\r");
		  	    }
	  	   }
	  	}
	  	return logger.toString();
    }
}
