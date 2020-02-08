package com.stone.studio.sandbox.util;

import com.google.gson.Gson;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ReportInfo {
    private Object request;
    private String server;
    private ArrayList<String> records = new ArrayList();
    private String url;
    private String method;
    private String remoteHost;
    private String userAgent;
    private String contentType;
    private Object parameters;
    private String current_sql;
    private static final int maxBodySize = 4096;
    private static Gson gson = new Gson();

    public ReportInfo() {
        this.reset();
    }

    public void reset() {
        this.request = null;
        this.url = "";
        this.method = "";
        this.remoteHost = "";
        this.contentType = "";
        this.userAgent = "";
        this.current_sql = "";
        this.parameters = null;
    }

    public ArrayList<String> getRecords() {
        return this.records;
    }

    public void setRecords(ArrayList<String> records) {
        this.records = records;
    }

    public String getUrl() throws Exception {
        Object result = CommonUtils.callMethod(this.request, "getRequestURL", new Object[0]);
        if (null == result) {
            return "";
        } else {
            this.url = result.toString();
            return this.url;
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUrl(Object url) {
        if (null != url) {
            this.url = url.toString();
        }
    }

    public String getMethod() throws Exception {
        Object result = CommonUtils.callMethod(this.request, "getMethod", new Object[0]);
        if (null == result) {
            return "";
        } else {
            this.method = result.toString();
            return this.method;
        }
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRemoteHost() throws Exception {
        Object result = CommonUtils.callMethod(this.request, "getRemoteAddr", new Object[0]);
        if (null == result) {
            return "";
        } else {
            this.remoteHost = result.toString();
            return this.remoteHost;
        }
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public String getUserAgent() throws Exception {
        if (null == this.request) {
            return "";
        } else {
            Method method = this.request.getClass().getMethod("getHeader", String.class);
            Object result = method.invoke(this.request, "User-Agent");
            if (null == result) {
                return "";
            } else {
                this.userAgent = result.toString();
                return this.userAgent;
            }
        }
    }

    public String getHeader(String key) throws Exception {
        if (null == this.request) {
            return "";
        } else {
            Method method = this.request.getClass().getMethod("getHeader", String.class);
            Object result = method.invoke(this.request, key);
            return null == result ? "" : result.toString();
        }
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Map<String, String> getHttpHeaders() throws Exception {
        Object result = CommonUtils.callMethod(this.request, "getHeaderNames", new Object[0]);
        if (null == result) {
            return null;
        } else {
            Map<String, String> map = new HashMap();
            Enumeration headerNames = (Enumeration) result;

            while (headerNames.hasMoreElements()) {
                String key = (String) headerNames.nextElement();
                if (!"cookie".equals(key)) {
                    map.put(key, this.getHeader(key));
                }
            }

            return map;
        }
    }

    public String getContentType() throws Exception {
        Object result = CommonUtils.callMethod(this.request, "getContentType", new Object[0]);
        if (null == result) {
            return "";
        } else {
            this.contentType = result.toString();
            return this.contentType;
        }
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Object getParameters() throws Exception {
        if (!"GET".equals(this.method) && !"application/x-www-form-urlencoded".equals(this.getContentType())) {
            // 一次http 请求中可能有多个sql，第二次调用时parameters是String，直接跳过
            boolean isByteArray = (null != this.parameters && this.parameters instanceof byte[]);
            boolean isCharArray = (null != this.parameters && this.parameters instanceof char[]);
            if (isByteArray) {
                int length = CommonUtils.getTrailZeroesIndex((byte[])this.parameters);
                this.parameters = new String((byte[]) this.parameters, 0, Math.min(length, maxBodySize));
            } else if (isCharArray) {
                int length = new String((char[])this.parameters).trim().length();
                this.parameters = new String((char[]) this.parameters, 0, Math.min(length, maxBodySize));
            } else {
                // just skip
                // DebugSupport.info(String.format("found non-byte-array:%s，type:%s!", this.parameters, this.parameters.getClass().getName()));
            }
        } else {
            Object result = CommonUtils.callMethod(this.request, "getParameterMap", new Object[0]);
            if (null == result) {
                return null;
            }
            this.parameters = gson.toJson((Map) result);
        }

        if (this.parameters == null) {
//            DebugSupport.warning(String.format("parameters is null, url:%s, method:%s", this.url, this.method));
            return "";
        } else {
            // DebugSupport.info(String.format("parameters:%s!", this.parameters.toString()));
            return CommonUtils.getURLEncoderString(this.parameters.toString());
        }
    }

    public void setParameters(Object params) {
        this.parameters = params;
    }

    public String getCurrent_sql() {
        return this.current_sql;
    }

    public void setCurrent_sql(String current_sql) {
        this.current_sql = current_sql;
    }

    public Object getRequest() {
        return this.request;
    }

    public void setRequest(Object request) {
        this.request = request;
    }

    public String getServer() {
        return this.server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
