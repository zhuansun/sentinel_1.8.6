package com.zspc.sentinel.util;

public class EnvUtil {

    public static String getNacosServerAddr(String env) {
        if ("local".equals(env)) {
            return "http://localhost:8848";
        } else if ("dev".equals(env)) {
            return "http://localhost:8848";
        } else if ("test".equals(env)) {
            return "http://localhost:8848";
        } else if ("prod".equals(env)) {
            return "http://localhost:8848";
        }
        return "http://localhost:8848";
    }

    public static String getNacosUserName(String env) {
        if ("local".equals(env)) {
            return "nacos";
        } else if ("dev".equals(env)) {
            return "nacos";
        } else if ("test".equals(env)) {
            return "nacos";
        } else if ("prod".equals(env)) {
            return "nacos";
        }
        return "nacos";
    }

    public static String getNacosPassword(String env) {
        if ("local".equals(env)) {
            return "nacos";
        } else if ("dev".equals(env)) {
            return "nacos";
        } else if ("test".equals(env)) {
            return "nacos";
        } else if ("prod".equals(env)) {
            return "nacos";
        }
        return "nacos";
    }
}
