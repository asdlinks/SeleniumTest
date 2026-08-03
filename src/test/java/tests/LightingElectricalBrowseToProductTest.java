package tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.CategoryPage;
import pages.ProductPage;
import support.CategoryProducts;
import support.Config;
import support.DriverFactory;

public class LightingElectricalBrowseToProductTest {

    private static final String CATEGORY = "lighting-electrical";
    private static final By PRODUCT_CARDS = By.cssSelector("main a[href*='/product-page/']");
    private static final By ADD_TO_CART_BUTTON = By.xpath(
            "//span[text()='Add to Cart'] | //button[contains(text(), 'Add to Cart')] | //button[contains(text(), 'ADD TO CART')]");
    private static final Pattern PRICE_PATTERN = Pattern.compile("\\$\\d+(?:\\.\\d{2})?");

    private WebDriver driver;
    private WebDriverWait wait;
    private CategoryPage categoryPage;
    private ProductPage productPage;

    @BeforeEach
    void setUp() {
        driver = DriverFactory.create();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        categoryPage = new CategoryPage(driver);
        productPage = new ProductPage(driver);
    }

    @AfterEach
    void tearDown() {
        DriverFactory.quit();
    }

    private By headingLocator(String heading) {
        String firstWord = heading.split(" ")[0];
        return By.xpath("//h1[contains(text(), '" + firstWord + "')] | //h2[contains(text(), '" + firstWord + "')]");
    }

    private List<String> extractPrices(String text) {
        List<String> prices = new java.util.ArrayList<>();
        Matcher matcher = PRICE_PATTERN.matcher(text);
        while (matcher.find()) {
            prices.add(matcher.group());
        }
        return prices;
    }

    private String bodyText() {
        return driver.findElement(By.tagName("body")).getText();
    }

    @Test
    void browsesLightingElectricalCategoryAndOpensProductDetail() {
        String path = CategoryProducts.path(CATEGORY);
        String expectedHeading = CategoryProducts.heading(CATEGORY);
        String productName = CategoryProducts.product(CATEGORY);

        driver.get(Config.baseUrl() + path);

        org.openqa.selenium.WebElement heading =
                wait.until(ExpectedConditions.visibilityOfElementLocated(headingLocator(expectedHeading)));
        assertTrue(heading.getText().contains(expectedHeading.split(" ")[0]),
                "Expected the category heading to reference " + expectedHeading);

        wait.until(ExpectedConditions.visibilityOfElementLocated(PRODUCT_CARDS));
        int productCount = driver.findElements(PRODUCT_CARDS).size();
        assertTrue(productCount > 0, "Expected the Lighting & Electrical product grid to render at least one product");

        List<String> listingPrices = extractPrices(bodyText());
        assertFalse(listingPrices.isEmpty(), "Expected product prices to be visible on the Lighting & Electrical listing");

        categoryPage.selectProduct(productName);

        wait.until(ExpectedConditions.visibilityOfElementLocated(ADD_TO_CART_BUTTON));
        assertTrue(productPage.isAddToCartVisible(), "Product page's Add to Cart button should be visible");

        String productPageBody = bodyText();
        assertTrue(productPageBody.contains(productName), "Expected product page to display the product name " + productName);

        List<String> productPagePrices = extractPrices(productPageBody);
        assertFalse(productPagePrices.isEmpty(), "Expected a price to be visible on the product page");
    }
}
