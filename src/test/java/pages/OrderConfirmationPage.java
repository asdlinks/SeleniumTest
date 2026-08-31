package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import support.Helpers;

import java.time.Duration;

public class OrderConfirmationPage extends BasePage {

    public OrderConfirmationPage(WebDriver driver) {
        super(driver);
    }

    public By getThankYouMessage() {
        return By.xpath("//h1[contains(translate(., 'THANK YOU', 'thank you'), 'thank you')]"
                + " | //h2[contains(translate(., 'THANK YOU', 'thank you'), 'thank you')]"
                + " | //span[contains(translate(., 'THANK YOU', 'thank you'), 'thank you')]"
                + " | //h1[contains(translate(., 'ORDER', 'order'), 'order')]"
                + " | //div[contains(translate(., 'ORDER CONFIRMED', 'order confirmed'), 'order confirmed')]");
    }

    public By getConfirmationDetails() {
        return By.xpath("//div[contains(@class, 'confirmation')] | //section[contains(@class, 'confirmation')]"
                + " | //div[contains(@class, 'order-details')]");
    }

    public void waitForConfirmationPageLoad() {
        waitFor(getThankYouMessage(), Duration.ofSeconds(30));
    }

    public String getThankYouText() {
        WebElement message = driver.findElement(getThankYouMessage());
        return message.getText();
    }

    public boolean isOrderConfirmed() {
        try {
            WebElement thankYouMsg = driver.findElement(getThankYouMessage());
            boolean isDisplayed = thankYouMsg.isDisplayed();
            String text = thankYouMsg.getText().toLowerCase();
            return isDisplayed && (text.contains("thank you") || text.contains("confirmed"));
        } catch (RuntimeException err) {
            return false;
        }
    }

    public String getOrderDetails() {
        try {
            return driver.findElement(getConfirmationDetails()).getText();
        } catch (RuntimeException err) {
            return "Order details not found";
        }
    }

    public boolean validateCustomerNameOnConfirmation(String firstName, String lastName) {
        String pageText = Helpers.bodyText(driver);
        return pageText.contains(firstName) && pageText.contains(lastName);
    }
}
