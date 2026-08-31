package support;

import org.openqa.selenium.json.Json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Single entry point for every piece of shared, non-secret test data used by
 * the suite (page titles, category names, product names, prices, SKUs,
 * customer details, filter limits) — read from
 * src/test/resources/shopbricks-data.json rather than duplicated inline
 * across test classes.
 */
@SuppressWarnings("unchecked")
public final class DataProvider {

    private static final String DATA_FILE = "shopbricks-data.json";
    private static final Map<String, Object> DATA = load();

    private DataProvider() {
    }

    private static Map<String, Object> load() {
        try (InputStream stream = DataProvider.class.getClassLoader().getResourceAsStream(DATA_FILE)) {
            if (stream == null) {
                throw new IllegalStateException(DATA_FILE + " not found on the classpath");
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return new Json().toType(reader, Map.class);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read " + DATA_FILE, e);
        }
    }

    public static Map<String, Object> site() {
        return (Map<String, Object>) DATA.get("site");
    }

    public static Map<String, Object> navigation() {
        return (Map<String, Object>) DATA.get("navigation");
    }

    public static Map<String, Object> categories() {
        return (Map<String, Object>) DATA.get("categories");
    }

    public static Map<String, Object> products() {
        return (Map<String, Object>) DATA.get("products");
    }

    public static Map<String, Object> productPage() {
        return (Map<String, Object>) DATA.get("productPage");
    }

    public static Map<String, Object> homepage() {
        return (Map<String, Object>) DATA.get("homepage");
    }

    public static Map<String, Object> testCases() {
        return (Map<String, Object>) DATA.get("testCases");
    }

    public static String baseUrl() {
        return (String) site().get("baseUrl");
    }

    /** Look up a category block by its key (for example 'hardware'). */
    public static Map<String, Object> getCategory(String key) {
        Map<String, Object> category = (Map<String, Object>) categories().get(key);

        if (category == null) {
            throw new IllegalArgumentException(
                    "Unknown category key '" + key + "'. Available keys: " + categories().keySet());
        }

        return category;
    }

    /** Look up a product block by its slug (for example 'pipe-wrench-8-in-length'). */
    public static Map<String, Object> getProduct(String slug) {
        Map<String, Object> product = (Map<String, Object>) products().get(slug);

        if (product == null) {
            throw new IllegalArgumentException("Unknown product slug '" + slug + "'. "
                    + products().size() + " products loaded.");
        }

        return product;
    }

    /** Expected products for a category listing page, in display order. */
    public static List<Map<String, Object>> getExpectedProducts(String key) {
        List<Map<String, Object>> expected = new ArrayList<>();
        List<Object> raw = (List<Object>) getCategory(key).get("expectedProducts");
        for (Object item : raw) {
            expected.add((Map<String, Object>) item);
        }
        return expected;
    }

    /** Data block driving a named test case (for example 'checkout'). */
    public static Map<String, Object> getTestCase(String key) {
        Map<String, Object> testCase = (Map<String, Object>) testCases().get(key);

        if (testCase == null) {
            throw new IllegalArgumentException(
                    "Unknown test case key '" + key + "'. Available keys: " + testCases().keySet());
        }

        return testCase;
    }

    public static double asDouble(Object value) {
        return ((Number) value).doubleValue();
    }

    public static int asInt(Object value) {
        return ((Number) value).intValue();
    }
}
