package support;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads src/test/resources/config.properties for the base URL and the
 * MAESTRO_CREDS environment variable for credentials, when a suite needs a
 * logged-in session. Never hard-code the site URL or a credential value in a
 * test class.
 *
 * This suite does not require a logged-in session (no test exercises a login
 * flow), so username()/password() are provided for completeness only.
 */
public final class Config {

    private static final Properties PROPERTIES = load();

    private Config() {
    }

    private static Properties load() {
        Properties properties = new Properties();

        try (InputStream stream = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read config.properties", e);
        }

        return properties;
    }

    public static String baseUrl() {
        return PROPERTIES.getProperty("base.url");
    }

    /** @return the username from MAESTRO_CREDS, or null when not set */
    public static String username() {
        return extractJsonField(System.getenv("MAESTRO_CREDS"), "username");
    }

    /** @return the password from MAESTRO_CREDS, or null when not set */
    public static String password() {
        return extractJsonField(System.getenv("MAESTRO_CREDS"), "password");
    }

    private static String extractJsonField(String rawJson, String field) {
        if (rawJson == null || rawJson.isBlank()) {
            return null;
        }

        Matcher matcher = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"").matcher(rawJson);
        return matcher.find() ? matcher.group(1) : null;
    }
}
