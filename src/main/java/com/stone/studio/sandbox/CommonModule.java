package com.stone.studio.sandbox;

import com.alibaba.jvm.sandbox.api.Module;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.stone.studio.sandbox.util.CommonUtils;
import com.stone.studio.sandbox.util.ReportInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CommonModule implements Module {
    private final static int TIMEOUT_LIMIT = 500;
    private Gson gson = new Gson();
    private static Cache<String, Boolean> cache;

    protected static ThreadLocal<ReportInfo> reportInfoThreadLocal = new ThreadLocal<ReportInfo>() {
        @Override
        protected ReportInfo initialValue() {
            return new ReportInfo();
        }
    };

    public static Cache<String, Boolean> getCache(){
        if(cache == null){
            cache = CacheBuilder.newBuilder().
                    concurrencyLevel(30).
                    expireAfterAccess(10, TimeUnit.MINUTES).
                    initialCapacity(10).
                    maximumSize(200).
                    build();
        }

        return cache;
    }

    protected double getStartTime() {
        return System.currentTimeMillis();
    }

    protected void recordExecuteTime(double time) {
        if(time >= TIMEOUT_LIMIT) {
            CommonUtils.recordExecuteTime();
        }
    }

    protected void detect(String level, String type, String className, String methodName, String methodParameters, String exception) {
        try {
            if (null != exception) {
                methodParameters = methodParameters + "\t" + exception;
            }
            Boolean result = cache.getIfPresent(methodParameters);
            if (null != result && result)
                return;
            cache.put(methodParameters, true);
            System.out.println(genJson(level, type, className, methodName, methodParameters, exception));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected String genJson(String level, String type, String className, String methodName, String methodParameters, String exception) throws Exception {
        double startTime = getStartTime();
        Map map = new HashMap();
        map.put("level", level);
        map.put("type", type);
        map.put("className", className);
        map.put("methodName", methodName);
        map.put("methodParameters", methodParameters);
        ReportInfo raspInfo = reportInfoThreadLocal.get();
        if (null != raspInfo) {
            if (null != raspInfo.getRequest()) {
                map.put("remoteHost", raspInfo.getRemoteHost());
                map.put("httpMethod", raspInfo.getMethod());
                map.put("url", raspInfo.getUrl());
                map.put("httpParameters", raspInfo.getParameters());
                map.put("headers", raspInfo.getHttpHeaders());
            }
        }

        map.put("ExceptionInfo", exception);
        map.put("stackTrace", getStackTrace());
        String json = gson.toJson(map);
        recordExecuteTime(System.currentTimeMillis() - startTime);
        return json;
    }

    protected ArrayList<String> getStackTrace() {
        ArrayList<String> stacks = new ArrayList<>();
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        boolean start = false; // 记录堆栈开始位置，rasp内部调用和反射省略
        for (StackTraceElement each : st) {
            String info = each.toString();
            if (info.startsWith("javax.servlet.http.HttpServlet.service")
                    || info.startsWith("org.apache.catalina.core.ApplicationFilterChain.internalDoFilter")) {
                break;
            }
            if (info.startsWith("java.lang.ProcessImpl")) {
                start = true;
            }
            if(start) {
                stacks.add(info);
            }
        }

        return stacks;
    }
}
