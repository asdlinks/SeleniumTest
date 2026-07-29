package support;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Config {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                PROPERTIES.load(in);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to load config.properties", e);
        }
    }

    private Config() {
    }

    public static String baseUrl() {
        return PROPERTIES.getProperty("base.url");
    }

    public static String username() {
        String creds = System.getenv("MAESTRO_CREDS");
        if (creds == null) {
            return null;
        }
        return extractJsonValue(creds, "username");
    }

    public static String password() {
        String creds = System.getenv("MAESTRO_CREDS");
        if (creds == null) {
            return null;
        }
        return extractJsonValue(creds, "password");
    }

    private static String extractJsonValue(String json, String key) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"")
                .matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }
}
