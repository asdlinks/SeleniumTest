import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Single entry point for every piece of test data used by the framework.
 *
 * All expected values (page titles, category names, product names, prices,
 * SKUs, customer details, filter limits) live in shopbricks-data.json.
 * Nothing in the step definitions or page objects should hard-code a value
 * that can be read from here.
 */
public final class DataProvider {

    private static final String DATA_FILE = "shopbricks-data.json";
    private static final JsonNode DATA = load();

    private DataProvider() {
    }

    private static JsonNode load() {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream stream = DataProvider.class.getClassLoader().getResourceAsStream(DATA_FILE)) {
            if (stream != null) {
                return mapper.readTree(stream);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read " + DATA_FILE + " from the classpath", e);
        }

        // Fallback for running outside Maven (the file lives beside this class)
        Path onDisk = Paths.get("shared-objects", DATA_FILE);
        try {
            if (Files.exists(onDisk)) {
                return mapper.readTree(Files.newInputStream(onDisk));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read " + onDisk, e);
        }

        throw new IllegalStateException(DATA_FILE + " not found on the classpath or at " + onDisk);
    }

    /** @return the whole parsed data file */
    public static JsonNode data() {
        return DATA;
    }

    public static JsonNode site() {
        return DATA.get("site");
    }

    public static JsonNode pages() {
        return DATA.get("pages");
    }

    public static JsonNode navigation() {
        return DATA.get("navigation");
    }

    public static JsonNode categories() {
        return DATA.get("categories");
    }

    public static JsonNode products() {
        return DATA.get("products");
    }

    public static JsonNode productPage() {
        return DATA.get("productPage");
    }

    public static JsonNode homePageContent() {
        return DATA.get("homePageContent");
    }

    public static JsonNode homepage() {
        return DATA.get("homepage");
    }

    public static JsonNode credentials() {
        return DATA.get("credentials");
    }

    public static JsonNode testCases() {
        return DATA.get("testCases");
    }

    public static String baseUrl() {
        return site().get("baseUrl").asText();
    }

    /**
     * Look up a category block by its key (for example 'hardware').
     *
     * @param key category key as used in the site URL
     * @return category data block
     */
    public static JsonNode getCategory(String key) {
        JsonNode category = categories().get(key);

        if (category == null) {
            throw new IllegalArgumentException(
                    "Unknown category key '" + key + "'. Available keys: " + String.join(", ", fieldNames(categories())));
        }

        return category;
    }

    /**
     * Look up a product block by its slug (for example 'pipe-wrench-8-in-length').
     *
     * @param slug product slug as used in the site URL
     * @return product data block
     */
    public static JsonNode getProduct(String slug) {
        JsonNode product = products().get(slug);

        if (product == null) {
            throw new IllegalArgumentException("Unknown product slug '" + slug + "'. "
                    + products().size() + " products loaded.");
        }

        return product;
    }

    /**
     * Look up a static page block by its key (for example 'home').
     *
     * @param key page key
     * @return page data block
     */
    public static JsonNode getPage(String key) {
        JsonNode page = pages().get(key);

        if (page == null) {
            throw new IllegalArgumentException(
                    "Unknown page key '" + key + "'. Available keys: " + String.join(", ", fieldNames(pages())));
        }

        return page;
    }

    /**
     * Fully qualified URL of a category listing page.
     *
     * @param key category key
     * @return absolute url
     */
    public static String getCategoryUrl(String key) {
        return getCategory(key).get("url").asText();
    }

    /**
     * Expected products for a category listing page, in display order.
     *
     * @param key category key
     * @return products with name, slug, sku, price and priceText
     */
    public static List<JsonNode> getExpectedProducts(String key) {
        List<JsonNode> expected = new ArrayList<>();
        getCategory(key).get("expectedProducts").forEach(expected::add);
        return expected;
    }

    /**
     * Expected product names for a category listing page, in display order.
     *
     * @param key category key
     * @return product names
     */
    public static List<String> getExpectedProductNames(String key) {
        List<String> names = new ArrayList<>();
        getExpectedProducts(key).forEach(product -> names.add(product.get("name").asText()));
        return names;
    }

    /**
     * Data block driving a named test case (for example 'checkout').
     *
     * @param key test case key
     * @return test case data block
     */
    public static JsonNode getTestCase(String key) {
        JsonNode testCase = testCases().get(key);

        if (testCase == null) {
            throw new IllegalArgumentException(
                    "Unknown test case key '" + key + "'. Available keys: " + String.join(", ", fieldNames(testCases())));
        }

        return testCase;
    }

    private static List<String> fieldNames(JsonNode node) {
        List<String> names = new ArrayList<>();
        Iterator<String> iterator = node.fieldNames();
        while (iterator.hasNext()) {
            names.add(iterator.next());
        }
        return names;
    }
}
