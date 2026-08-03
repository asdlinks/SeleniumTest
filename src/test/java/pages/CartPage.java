package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CartPage extends BasePage {

    private static final By CHECKOUT_BUTTON = By.xpath(
            "//button[contains(., 'Checkout')] | //a[contains(., 'Checkout')]");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public void proceedToCheckout() {
        WebElement checkoutBtn = waitForClickable(CHECKOUT_BUTTON);
        scrollIntoView(checkoutBtn);
        checkoutBtn.click();
    }
}
