package sys;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Author: Sergey42
 * Date: 29.10.13 12:34
 */
public class Messages {

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

    private String getMessage(String key) {
        String message = properties.getProperty(key);
        return message == null ? key : message;
    }
}
