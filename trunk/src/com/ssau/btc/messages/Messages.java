package com.ssau.btc.messages;

import com.intelli.ray.core.Inject;
import com.intelli.ray.core.ManagedComponent;
import com.ssau.btc.utils.LocaleHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Author: Sergey42
 * Date: 29.10.13 12:34
 */
@ManagedComponent(name = "Messages")
public class Messages {

    @Inject
    protected LocaleHelper localeHelper;

    protected Properties properties;

    public void init(String locale) {
        String suffix = "_" + localeHelper.safeLang(locale).toLowerCase();
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

    public String getMessage(String key) {
        String message = properties.getProperty(key);
        return message == null ? key : message;
    }

    public String formatMessage(String key, Object... args) {
        String message = properties.getProperty(key);
        return message == null ? key : String.format(message, args);
    }
}
