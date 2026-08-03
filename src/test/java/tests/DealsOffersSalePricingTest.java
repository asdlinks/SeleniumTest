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
import pages.DealsAndOffersPage;
import pages.ProductPage;
import support.Config;
import support.DriverFactory;

public class DealsOffersSalePricingTest {

    private static final By ADD_TO_CART_BUTTON = By.xpath(
            "//span[text()='Add to Cart'] | //button[contains(text(), 'Add to Cart')] | //button[contains(text(), 'ADD TO CART')]");
    private static final Pattern PRICE_PATTERN = Pattern.compile("\\$\\d+(?:\\.\\d{2})?");

    private WebDriver driver;
    private WebDriverWait wait;
    private DealsAndOffersPage dealsAndOffersPage;
    private CategoryPage categoryPage;
    private ProductPage productPage;

    @BeforeEach
    void setUp() {
        driver = DriverFactory.create();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        dealsAndOffersPage = new DealsAndOffersPage(driver);
        categoryPage = new CategoryPage(driver);
        productPage = new ProductPage(driver);
    }

    @AfterEach
    void tearDown() {
        DriverFactory.quit();
    }

    private List<String> extractPrices(String text) {
        List<String> prices = new java.util.ArrayList<>();
        Matcher matcher = PRICE_PATTERN.matcher(text);
        while (matcher.find()) {
            prices.add(matcher.group());
        }
        return prices;
    }

    @Test
    void showsSalePricingConsistentlyAcrossListingAndProductPage() {
        driver.get(Config.baseUrl() + "category/deals-offers");
        dealsAndOffersPage.waitForPageLoad();
        assertTrue(dealsAndOffersPage.isDealsOffersPageLoaded(), "Deals & Offers page should have loaded");

        int initialCount = dealsAndOffersPage.getProductCardCount();
        assertTrue(initialCount > 0, "Expected at least one product card in the Deals & Offers grid");

        String bodyText = dealsAndOffersPage.getFilterRangeText();
        assertTrue(bodyText.toLowerCase().contains("sale"), "Expected 'Sale' badge text to appear on the listing");

        List<String> listingPrices = dealsAndOffersPage.getProductPrices();
        assertTrue(listingPrices.size() >= initialCount,
                "Expected at least one regular/sale price pair per discounted product");

        dealsAndOffersPage.clickLoadMore();
        int countAfterLoadMore = dealsAndOffersPage.getProductCardCount();
        assertTrue(countAfterLoadMore > initialCount,
                "Expected more discounted products to load after clicking Load More");

        List<String> pricesBeforeNavigating = dealsAndOffersPage.getProductPrices();

        categoryPage.selectProduct("Anchoring Cement (25 Lb.)");

        wait.until(ExpectedConditions.visibilityOfElementLocated(ADD_TO_CART_BUTTON));
        assertTrue(productPage.isAddToCartVisible(), "Product page's Add to Cart button should be visible");

        String productPageBody = driver.findElement(By.tagName("body")).getText();
        List<String> productPagePrices = extractPrices(productPageBody);
        assertFalse(productPagePrices.isEmpty(), "Expected at least one price on the product page");

        boolean priceMatchesListing = productPagePrices.stream().anyMatch(pricesBeforeNavigating::contains);
        assertTrue(priceMatchesListing,
                "Expected the product page's discounted price to match the price shown on the listing");
    }
}
