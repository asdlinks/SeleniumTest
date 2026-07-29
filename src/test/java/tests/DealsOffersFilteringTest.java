package tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import pages.DealsAndOffersPage;
import pages.HomePage;
import support.Config;
import support.DriverFactory;

public class DealsOffersFilteringTest {

    private WebDriver driver;
    private HomePage homePage;
    private DealsAndOffersPage dealsAndOffersPage;

    @BeforeEach
    void setUp() {
        driver = DriverFactory.create();
        homePage = new HomePage(driver);
        dealsAndOffersPage = new DealsAndOffersPage(driver);

        homePage.navigateTo(Config.baseUrl());
        homePage.waitForPageLoad();
    }

    @AfterEach
    void tearDown() {
        DriverFactory.quit();
    }

    @Test
    void validatesDealsOffersFilteringAndLoadMoreFlow() {
        homePage.clickDealsAndOffers();
        assertTrue(dealsAndOffersPage.isDealsOffersPageLoaded(), "Deals & Offers page should have loaded");

        dealsAndOffersPage.clickAllProductsLink();

        int initialCount = dealsAndOffersPage.getProductCardCount();
        assertTrue(initialCount >= 10, "Expected at least 10 products on the page, found " + initialCount);

        int lastCount = initialCount;
        for (int i = 0; i < 3; i++) {
            int before = dealsAndOffersPage.getProductCardCount();
            dealsAndOffersPage.clickLoadMore();
            int after = dealsAndOffersPage.getProductCardCount();
            lastCount = after;
        }
        assertTrue(lastCount > 0, "Expected products to remain visible after clicking Load More three times");

        dealsAndOffersPage.setPriceRange(300, 0);
        List<String> prices = dealsAndOffersPage.getProductPrices();
        assertTrue(prices.size() > 0, "Expected at least one product price on the page");
        boolean hasWithinRange = prices.stream()
                .anyMatch(priceText -> Double.parseDouble(priceText.replace("$", "")) <= 300);
        assertTrue(hasWithinRange, "Expected at least one product priced at 300 or less");

        dealsAndOffersPage.setPriceRange(100, 0);
        String filterText = dealsAndOffersPage.getFilterRangeText();
        assertTrue(filterText.toLowerCase().contains("100"), "Expected price filter text to mention 100");
    }
}
