package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import pages.CartPage;
import pages.CategoryPage;
import pages.CheckoutPage;
import pages.DealsAndOffersPage;
import pages.HomePage;
import pages.OrderConfirmationPage;
import pages.ProductPage;
import support.Config;
import support.DataProvider;
import support.DriverFactory;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Converted from features/shopbricks-checkout.feature.
 *
 * Navigates to Deals & Offers, selects a Hardware product (pipe wrench),
 * adds it to the cart and completes checkout, then verifies the order
 * confirmation.
 */
class ShopbricksCheckoutTest {

    private static final Map<String, Object> CHECKOUT_DATA = DataProvider.getTestCase("checkout");

    private WebDriver driver;
    private HomePage homePage;
    private DealsAndOffersPage dealsAndOffersPage;
    private CategoryPage categoryPage;
    private ProductPage productPage;
    private CartPage cartPage;
    private CheckoutPage checkoutPage;
    private OrderConfirmationPage orderConfirmationPage;

    @BeforeEach
    void setUp() {
        driver = DriverFactory.create();
        homePage = new HomePage(driver);
        dealsAndOffersPage = new DealsAndOffersPage(driver);
        categoryPage = new CategoryPage(driver);
        productPage = new ProductPage(driver);
        cartPage = new CartPage(driver);
        checkoutPage = new CheckoutPage(driver);
        orderConfirmationPage = new OrderConfirmationPage(driver);
    }

    @AfterEach
    void tearDown() {
        DriverFactory.quit();
    }

    @Test
    void completeCheckoutFlowForPipeWrenchPurchase() {
        homePage.navigateToHome(Config.baseUrl());
        homePage.waitForPageLoad();

        homePage.clickDealsAndOffers();
        dealsAndOffersPage.waitForPageLoad();
        assertTrue(dealsAndOffersPage.isDealsOffersPageLoaded(), "Deals & Offers page should have loaded");

        String categoryName = (String) CHECKOUT_DATA.get("categoryName");
        dealsAndOffersPage.selectCategory(categoryName);
        assertTrue(categoryPage.isCategoryPageLoaded(), "Hardware category page should have loaded");

        Map<String, Object> productData = (Map<String, Object>) CHECKOUT_DATA.get("product");
        String searchTerm = (String) productData.get("searchTerm");
        categoryPage.selectProduct(searchTerm);
        assertTrue(productPage.isAddToCartVisible(), "Add to Cart button should be visible on the product page");

        long aiAssistantTimeoutMs = ((Number) DataProvider.productPage().get("aiAssistantTimeoutMs")).longValue();
        productPage.addToCart(Duration.ofMillis(aiAssistantTimeoutMs));

        Map<String, Object> customerData = (Map<String, Object>) CHECKOUT_DATA.get("customer");
        cartPage.proceedToCheckout();
        try {
            checkoutPage.waitForCheckoutPageLoad();
        } catch (RuntimeException err) {
            System.out.println("Checkout page title not found, proceeding anyway");
        }
        checkoutPage.waitForCustomerDetailsForm();

        CheckoutPage.CustomerDetails customerDetails = new CheckoutPage.CustomerDetails();
        customerDetails.email = (String) customerData.get("email");
        customerDetails.firstName = (String) customerData.get("firstName");
        customerDetails.lastName = (String) customerData.get("lastName");
        customerDetails.address1 = (String) customerData.get("address1");
        customerDetails.city = (String) customerData.get("city");
        customerDetails.country = (String) customerData.get("country");
        customerDetails.region = (String) customerData.get("region");
        customerDetails.zip = (String) customerData.get("zip");
        customerDetails.phone = (String) customerData.get("phone");

        checkoutPage.fillCustomerDetails(customerDetails);
        checkoutPage.placeOrder();

        orderConfirmationPage.waitForConfirmationPageLoad();
        assertTrue(orderConfirmationPage.isOrderConfirmed(), "Order confirmation (Thank You) message should be visible");

        String orderDetails = orderConfirmationPage.getOrderDetails();
        assertFalse(orderDetails.isEmpty(), "Order confirmation details should be visible on the page");
    }
}
