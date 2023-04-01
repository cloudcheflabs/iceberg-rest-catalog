package com.cloudcheflabs.iceberg.catalog.rest.util;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;

public class StringUtils {

    public static InputStream readFile(String filePath) {
        try {
            return new FileInputStream(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream readFileFromClasspath(String filePath) {
        try {
            return new ClassPathResource(filePath).getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String fileToString(String filePath, boolean fromClasspath) {
        try {
            return fromClasspath ? IOUtils.toString(readFileFromClasspath(filePath)) :
                    IOUtils.toString(readFile(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Properties stringToProperties(String propertiesString) {
        try {
            Properties properties = new Properties();
            properties.load(new ByteArrayInputStream(propertiesString.getBytes()));
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static String getEnv(String key) {
        Map<String, String> envMap = System.getenv();
        return envMap.get(key);
    }

    public static void setEnv(String key, String value) {
        try {
            Map<String, String> env = System.getenv();
            Class<?> cl = env.getClass();
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            writableEnv.put(key, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set environment variable", e);
        }
    }
}
