import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

public class CartPage {

    private final WebDriver driver;

    public CartPage(WebDriver driver) {
        this.driver = driver;
    }

    // Locators
    public By getCartTitle() {
        return By.xpath("//h1[contains(text(), 'Cart')] | //h1[contains(text(), 'Shopping')]"
                + " | //span[contains(text(), 'Cart')]");
    }

    public By getCheckoutButton() {
        return By.xpath("//button[contains(text(), 'Checkout')] | //a[contains(text(), 'Checkout')]"
                + " | //span[text()='Checkout']");
    }

    public By getContinueShoppingButton() {
        return By.xpath("//button[contains(text(), 'Continue')] | //a[contains(text(), 'Continue')]");
    }

    public By getCartItemCount() {
        return By.xpath("//div[contains(@class, 'cart-count')] | //span[contains(@class, 'item-count')]");
    }

    public By getProductInCart(String productName) {
        return By.xpath("//div[contains(text(), '" + productName + "')]"
                + " | //span[contains(text(), '" + productName + "')]");
    }

    public By getCartSubtotal() {
        return By.xpath("//span[contains(text(), 'Subtotal')] | //div[contains(text(), 'Subtotal')]");
    }

    public By getRemoveItemButton() {
        return By.xpath("//button[contains(text(), 'Remove')] | //a[contains(text(), 'Remove')]");
    }

    // Actions
    public void proceedToCheckout() {
        // Wait for checkout button to be clickable
        WebElement checkoutBtn = Helpers.wait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(getCheckoutButton()));
        Helpers.scrollToElement(driver, checkoutBtn);
        Helpers.sleep(1000);
        checkoutBtn.click();
    }

    public void waitForCartPageLoad() {
        Helpers.wait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(getCartTitle()));
    }

    public boolean isProductInCart(String productName) {
        return Helpers.isVisible(driver, getProductInCart(productName));
    }

    public String getItemCount() {
        try {
            return driver.findElement(getCartItemCount()).getText();
        } catch (RuntimeException err) {
            return "0";
        }
    }

    public boolean isCheckoutButtonVisible() {
        return Helpers.isVisible(driver, getCheckoutButton());
    }
}
