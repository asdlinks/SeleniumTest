package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import pages.DealsAndOffersPage;
import pages.HomePage;
import support.Config;
import support.DataProvider;
import support.DriverFactory;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Converted from features/deals-offers-filtering.feature.
 *
 * Validates the Deals & Offers product listing behavior: filtering,
 * pagination (Load More) and price-range filter behavior.
 */
class DealsOffersFilteringTest {

    private static final Map<String, Object> DEALS_DATA = DataProvider.getTestCase("dealsOffersFiltering");

    private WebDriver driver;
    private HomePage homePage;
    private DealsAndOffersPage dealsAndOffersPage;

    @BeforeEach
    void setUp() {
        driver = DriverFactory.create();
        homePage = new HomePage(driver);
        dealsAndOffersPage = new DealsAndOffersPage(driver);
    }

    @AfterEach
    void tearDown() {
        DriverFactory.quit();
    }

    @Test
    void validateDealsOffersProductFilteringAndLoadMoreFlow() {
        homePage.navigateToHome(Config.baseUrl());
        homePage.waitForPageLoad();

        homePage.clickDealsAndOffers();
        dealsAndOffersPage.waitForPageLoad();
        assertTrue(dealsAndOffersPage.isDealsOffersPageLoaded(), "Deals & Offers page should have loaded");

        dealsAndOffersPage.clickAllProductsLink();

        int minimumProducts = DataProvider.asInt(DEALS_DATA.get("minimumProducts"));
        int countAfterAllProducts = dealsAndOffersPage.getProductCardCount();
        assertTrue(countAfterAllProducts >= minimumProducts,
                "Expected at least " + minimumProducts + " products, found " + countAfterAllProducts);

        int loadMoreClicks = DataProvider.asInt(DEALS_DATA.get("loadMoreClicks"));
        int lastCount = countAfterAllProducts;
        for (int i = 0; i < loadMoreClicks; i++) {
            dealsAndOffersPage.clickLoadMore();
            lastCount = dealsAndOffersPage.getProductCardCount();
        }
        assertTrue(lastCount > 0, "Product count should be greater than zero after clicking Load More");

        Map<String, Object> priceFilter = (Map<String, Object>) DEALS_DATA.get("priceFilter");
        int minimum = DataProvider.asInt(priceFilter.get("minimum"));
        int firstMaximum = DataProvider.asInt(priceFilter.get("firstMaximum"));
        int secondMaximum = DataProvider.asInt(priceFilter.get("secondMaximum"));

        dealsAndOffersPage.setPriceRange(firstMaximum, minimum);

        List<String> prices = dealsAndOffersPage.getProductPrices();
        assertFalse(prices.isEmpty(), "Product prices should be present after filtering");

        boolean hasWithinRange = prices.stream()
                .anyMatch(priceText -> Double.parseDouble(priceText.replace("$", "")) <= (double) firstMaximum);
        assertTrue(hasWithinRange, "At least one product should be priced at " + firstMaximum + " or less");

        dealsAndOffersPage.setPriceRange(secondMaximum, minimum);

        String filterRangeText = dealsAndOffersPage.getFilterRangeText();
        assertTrue(filterRangeText.toLowerCase().contains(String.valueOf(secondMaximum)),
                "Filter range text should mention the second maximum price " + secondMaximum);
    }
}
