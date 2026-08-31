package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.WebDriver;
import pages.CatalogPage;
import pages.ProductPage;
import support.DataProvider;
import support.DriverFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Converted from features/catalog-data-validation.feature.
 *
 * Data driven storefront validation: page titles, catalogue contents,
 * product details and navigation are validated against the expected data
 * held in src/test/resources/shopbricks-data.json.
 */
class CatalogDataValidationTest {

    private WebDriver driver;
    private CatalogPage catalogPage;
    private ProductPage productPage;

    @BeforeEach
    void setUp() {
        driver = DriverFactory.create();
        catalogPage = new CatalogPage(driver);
        productPage = new ProductPage(driver);
    }

    @AfterEach
    void tearDown() {
        DriverFactory.quit();
    }

    @ParameterizedTest(name = "category catalogue for {0} matches expected data")
    @ValueSource(strings = { "hardware", "power-tools", "lighting-electrical", "deals-offers" })
    void validateCategoryCatalogueAgainstExpectedData(String categoryKey) {
        Map<String, Object> category = DataProvider.getCategory(categoryKey);

        catalogPage.navigateTo((String) category.get("url"));
        catalogPage.waitForPageLoad();
        catalogPage.waitForProductGrid();

        String actualTitle = catalogPage.getPageTitle();
        assertEquals(category.get("pageTitle"), actualTitle,
                "Page title should match the expected title for '" + categoryKey + "'");

        int expectedMinimum = DataProvider.asInt(category.get("minimumExpectedProducts"));
        int actualCount = catalogPage.getProductCount();
        assertTrue(actualCount >= expectedMinimum,
                "Expected at least " + expectedMinimum + " products for '" + categoryKey + "', found " + actualCount);

        List<Map<String, Object>> expectedProducts = DataProvider.getExpectedProducts(categoryKey);
        List<CatalogPage.ListedProduct> listedProducts = catalogPage.getListedProducts();

        List<String> listedSlugs = new ArrayList<>();
        listedProducts.forEach(product -> listedSlugs.add(product.slug));

        List<String> missing = new ArrayList<>();
        for (Map<String, Object> product : expectedProducts) {
            if (!listedSlugs.contains(product.get("slug"))) {
                missing.add((String) product.get("name"));
            }
        }
        assertTrue(missing.isEmpty(), "Products missing from the '" + categoryKey + "' listing: " + missing);

        Map<String, CatalogPage.ListedProduct> listedBySlug = new java.util.LinkedHashMap<>();
        listedProducts.forEach(product -> listedBySlug.put(product.slug, product));

        List<String> mismatches = new ArrayList<>();
        for (Map<String, Object> expectedProduct : expectedProducts) {
            String slug = (String) expectedProduct.get("slug");
            String name = (String) expectedProduct.get("name");
            CatalogPage.ListedProduct listedProduct = listedBySlug.get(slug);

            if (listedProduct == null) {
                mismatches.add(name + ": not rendered");
                continue;
            }

            if (!listedProduct.name.equals(name)) {
                mismatches.add(slug + ": name '" + listedProduct.name + "' != expected '" + name + "'");
            }

            double expectedPrice = DataProvider.asDouble(expectedProduct.get("price"));
            if (listedProduct.price == null || Double.compare(listedProduct.price, expectedPrice) != 0) {
                mismatches.add(name + ": price " + listedProduct.priceText + " != expected "
                        + expectedProduct.get("priceText"));
            }
        }
        assertTrue(mismatches.isEmpty(), "Product data mismatches on the '" + categoryKey + "' listing: " + mismatches);
    }

    @ParameterizedTest(name = "product details for {0} match expected data")
    @ValueSource(strings = { "pipe-wrench-8-in-length", "steel-grip-claw-hammer", "black-desk-lamp",
            "shawns-20-volt-brushed-cordless-compact-drill" })
    void validateProductDetailsAgainstExpectedData(String slug) {
        Map<String, Object> product = DataProvider.getProduct(slug);

        productPage.navigateTo((String) product.get("url"));
        productPage.waitForProductDetailsLoad();

        assertEquals(product.get("pageTitle"), productPage.getPageTitle(),
                "Product page title should match the expected title for '" + slug + "'");

        assertEquals(product.get("name"), productPage.getDisplayedName(),
                "Displayed product name should match expected data for '" + slug + "'");
        assertEquals(DataProvider.asDouble(product.get("price")), productPage.getDisplayedPrice(), 0.001,
                "Displayed price should match expected data for '" + slug + "'");
        assertEquals(product.get("sku"), productPage.getDisplayedSku(),
                "Displayed SKU should match expected data for '" + slug + "'");

        assertTrue(productPage.isAddToCartButtonPresent(),
                "Add to Cart button should be available on the product page for '" + slug + "'");
    }

    @org.junit.jupiter.api.Test
    void validateBrowseBySidebarAndPriceFilterBoundsAgainstExpectedData() {
        Map<String, Object> category = DataProvider.getCategory("all-products");

        catalogPage.navigateTo((String) category.get("url"));
        catalogPage.waitForPageLoad();
        catalogPage.waitForProductGrid();

        Map<String, Object> navigation = DataProvider.navigation();
        Map<String, Object> browseBy = (Map<String, Object>) navigation.get("browseBy");

        String expectedHeading = (String) browseBy.get("heading");
        assertEquals(expectedHeading, catalogPage.getBrowseByHeadingText(),
                "Sidebar heading should match the expected value from the data file");

        List<Object> expectedCategoriesRaw = (List<Object>) browseBy.get("categories");
        List<String> expected = new ArrayList<>();
        for (Object item : expectedCategoriesRaw) {
            Map<String, Object> entry = (Map<String, Object>) item;
            expected.add(entry.get("label") + " -> " + stripTrailingSlash((String) entry.get("url")));
        }

        List<String> actual = new ArrayList<>();
        catalogPage.getSidebarCategories()
                .forEach(cat -> actual.add(cat.label + " -> " + stripTrailingSlash(cat.url)));

        Assertions.assertEquals(expected, actual, "Browse by list should match all expected categories in order");

        Map<String, Object> priceFilter = (Map<String, Object>) category.get("priceFilter");
        CatalogPage.PriceBounds actualBounds = catalogPage.getPriceFilterBounds();

        assertEquals(DataProvider.asInt(priceFilter.get("minimum")), actualBounds.minimum,
                "Price filter minimum should match expected range for 'all-products'");
        assertEquals(DataProvider.asInt(priceFilter.get("maximum")), actualBounds.maximum,
                "Price filter maximum should match expected range for 'all-products'");
    }

    private static String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
