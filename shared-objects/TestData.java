/**
 * Login credentials.
 *
 * Sourced from shopbricks-data.json (the `credentials` block).
 */
public final class TestData {

    private TestData() {
    }

    public static String username() {
        return DataProvider.credentials().get("username").asText();
    }

    public static String password() {
        return DataProvider.credentials().get("password").asText();
    }
}
