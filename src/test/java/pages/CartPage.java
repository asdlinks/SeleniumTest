package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import support.Helpers;

import java.time.Duration;

public class CartPage extends BasePage {

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public By getCheckoutButton() {
        return By.xpath("//button[contains(., 'Checkout')] | //a[contains(., 'Checkout')]"
                + " | //span[normalize-space(.)='Checkout']");
    }

    public void proceedToCheckout() {
        WebElement checkoutBtn = waitForClickable(getCheckoutButton(), Duration.ofSeconds(25));
        Helpers.scrollToElement(driver, checkoutBtn);
        checkoutBtn.click();
    }
}
