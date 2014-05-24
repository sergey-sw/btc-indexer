package com.ssau.btc.sys;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Author: Sergey42
 * Date: 29.10.13 12:34
 */
public class Messages {

    //TODO config
    static {
        init("ru");
    }

    private static Messages instance;

    private Properties properties;

    private Messages(String locale) {
        String suffix = locale.startsWith("ru") ? "_ru" : "";
        InputStream stream = getClass().getResourceAsStream("messages" + suffix + ".properties");
        try {
            InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
            properties = new Properties();
            properties.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (IOException e) {
                    //
                }
        }
    }

    public static void init(String locale) {
        instance = new Messages(locale);
    }

    public static String get(String key) {
        if (instance == null)
            throw new IllegalStateException("Messages not initialized");
        return instance.getMessage(key);
    }

    public static String format(String key, Object... args) {
        if (instance == null)
            throw new IllegalStateException("Messages not initialized");
        return instance.formatMessage(key, args);
    }

    private String getMessage(String key) {
        String message = properties.getProperty(key);
        return message == null ? key : message;
    }

    protected String formatMessage(String key, Object... args) {
        String message = properties.getProperty(key);
        return message == null ? key : String.format(message, args);
    }
}
