package com.stone.studio.sandbox.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CommonUtils {

    private final static String ENCODE = "UTF-8";

    public static Object callMethod(Object obj, String methodName, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (null == obj)
            return null;
        Method method = obj.getClass().getMethod(methodName);
        return method.invoke(obj, args);
    }

    /**
     * 获取最后一个非0结尾字符
     * @param data
     * @return
     */
    public static int getTrailZeroesIndex(byte[] data) {
        int i;
        for (i = data.length - 1; i > 0; i--) {
            if (data[i] != '\0') {
                break;
            }
        }
        return i + 1;
    }

    /**
     * URL 转码
     */
    public static String getURLEncoderString(String str) throws Exception {
        String result = "";
        if (null == str) {
            return "";
        }
        result = java.net.URLEncoder.encode(str, ENCODE);
        return result;
    }

    public static void recordExecuteTime() {
        System.out.println("time out occur");
    }
}
