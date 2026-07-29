package pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class OrderConfirmationPage extends BasePage {

    private static final By THANK_YOU_MESSAGE = By.xpath(
            "//h1[contains(text(), 'Thank You')] | //h2[contains(text(), 'Thank You')] | "
                    + "//span[contains(text(), 'Thank You')] | //h1[contains(text(), 'Order')] | "
                    + "//div[contains(text(), 'Order Confirmed')]");

    public OrderConfirmationPage(WebDriver driver) {
        super(driver);
    }

    public void waitForConfirmationPageLoad() {
        wait.withTimeout(Duration.ofSeconds(15))
                .until(ExpectedConditions.visibilityOfElementLocated(THANK_YOU_MESSAGE));
    }

    public boolean isOrderConfirmed() {
        try {
            WebElement message = driver.findElement(THANK_YOU_MESSAGE);
            String text = message.getText().trim().toLowerCase();
            return message.isDisplayed()
                    && (text.contains("thank you") || text.contains("confirmed"));
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    public String getThankYouText() {
        return driver.findElement(THANK_YOU_MESSAGE).getText().trim();
    }

    public String getOrderDetails() {
        return (String) ((JavascriptExecutor) driver).executeScript("return document.body.innerText;");
    }
}
