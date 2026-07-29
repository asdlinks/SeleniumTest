package tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
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
import support.DriverFactory;

public class ShopbricksCheckoutTest {

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

        homePage.navigateTo(Config.baseUrl());
        homePage.waitForPageLoad();
    }

    @AfterEach
    void tearDown() {
        DriverFactory.quit();
    }

    @Test
    void completesCheckoutFlowForPipeWrenchPurchase() {
        homePage.clickDealsAndOffers();
        assertTrue(dealsAndOffersPage.isDealsOffersPageLoaded(), "Deals & Offers page should have loaded");

        dealsAndOffersPage.selectCategory("Hardware");
        assertTrue(categoryPage.isCategoryPageLoaded(), "Hardware category page should have loaded");

        categoryPage.selectProduct("Pipe Wrench");
        assertTrue(productPage.isAddToCartVisible(), "Product page's Add to Cart button should be visible");

        productPage.addToCart();

        cartPage.proceedToCheckout();

        Map<String, String> customerDetails = new LinkedHashMap<>();
        customerDetails.put("email", "test@example.com");
        customerDetails.put("firstName", "John");
        customerDetails.put("lastName", "Doe");
        customerDetails.put("address1", "123 Main St");
        customerDetails.put("city", "New York");
        customerDetails.put("country", "United States");
        customerDetails.put("region", "Alaska");
        customerDetails.put("zip", "10001");
        customerDetails.put("phone", "5551234567");

        checkoutPage.fillCustomerDetails(customerDetails);
        checkoutPage.placeOrder();

        orderConfirmationPage.waitForConfirmationPageLoad();
        assertTrue(orderConfirmationPage.isOrderConfirmed(), "Order confirmation should show a thank you message");

        String thankYouText = orderConfirmationPage.getThankYouText();
        assertFalse(thankYouText.isEmpty(), "Thank you message text should not be empty");

        String orderDetails = orderConfirmationPage.getOrderDetails();
        assertFalse(orderDetails.isEmpty(), "Order confirmation details should not be empty");
    }
}
