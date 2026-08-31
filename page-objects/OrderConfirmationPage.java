import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderConfirmationPage {

    /** Set by the "extract and store the order number" step, replacing global.storedOrderNumber. */
    public static String storedOrderNumber;

    private final WebDriver driver;

    public OrderConfirmationPage(WebDriver driver) {
        this.driver = driver;
    }

    // Locators
    public By getThankYouMessage() {
        return By.xpath("//h1[contains(text(), 'Thank You')] | //h2[contains(text(), 'Thank You')]"
                + " | //span[contains(text(), 'Thank You')]"
                + " | //h1[contains(text(), 'Order')] | //div[contains(text(), 'Order Confirmed')]");
    }

    public By getOrderNumber() {
        return By.xpath("//span[contains(text(), 'Order')] | //div[contains(@class, 'order-number')]"
                + " | //p[contains(text(), '#')]");
    }

    public By getConfirmationDetails() {
        return By.xpath("//div[contains(@class, 'confirmation')] | //section[contains(@class, 'confirmation')]"
                + " | //div[contains(@class, 'order-details')]");
    }

    public By getContinueShoppingButton() {
        return By.xpath("//button[contains(text(), 'Continue')] | //a[contains(text(), 'Shop')]"
                + " | //button[contains(text(), 'Back')]");
    }

    public By getOrderStatus() {
        return By.xpath("//span[contains(@class, 'status')] | //div[contains(@class, 'status')]");
    }

    public By getEmailConfirmation() {
        return By.xpath("//span[contains(text(), 'email')] | //p[contains(text(), 'email')]"
                + " | //text()[contains(translate(., 'CONFIRMATION EMAIL', 'confirmation email'), 'confirmation email')]/..");
    }

    public By getFirstNameText() {
        return By.xpath("//span[contains(text(), 'John')] | //p[contains(text(), 'John')]"
                + " | //div[contains(text(), 'first') or contains(text(), 'First')]");
    }

    public By getLastNameText() {
        return By.xpath("//span[contains(text(), 'Doe')] | //p[contains(text(), 'Doe')]"
                + " | //div[contains(text(), 'last') or contains(text(), 'Last')]");
    }

    public By getConfirmationEmailMessage() {
        return By.xpath("//text()[contains(translate(., 'CONFIRMATION EMAIL SOON', 'confirmation email soon'), "
                + "'confirmation email soon')]/.."
                + " | //p[contains(translate(., 'CONFIRMATION', 'confirmation'), 'confirmation') "
                + "and contains(translate(., 'EMAIL', 'email'), 'email')]"
                + " | //div[contains(translate(., 'CONFIRMATION', 'confirmation'), 'confirmation')]");
    }

    // Actions
    public void waitForConfirmationPageLoad() {
        Helpers.wait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.visibilityOfElementLocated(getThankYouMessage()));
    }

    public String getThankYouText() {
        WebElement message = Helpers.wait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(getThankYouMessage()));
        return message.getText();
    }

    public boolean isThankYouMessageVisible() {
        return Helpers.isVisible(driver, getThankYouMessage());
    }

    public String getOrderDetails() {
        try {
            return driver.findElement(getConfirmationDetails()).getText();
        } catch (RuntimeException err) {
            return "Order details not found";
        }
    }

    public String extractOrderNumber() {
        try {
            Helpers.wait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(getOrderNumber()));
            String pageText = Helpers.bodyText(driver);

            // Try to extract order number from the page text
            // Look for patterns like "Order #123456" or "Order Number: 123456"
            List<Pattern> orderPatterns = Arrays.asList(
                    Pattern.compile("Order\\s*#?(\\d+)", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("Order\\s*Number[:\\s]+(\\d+)", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("Ref\\s*#?(\\d+)", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("#(\\d{6,})"));

            for (Pattern pattern : orderPatterns) {
                Matcher match = pattern.matcher(pageText);
                if (match.find() && match.group(1) != null) {
                    System.out.println("Extracted Order Number: " + match.group(1));
                    return match.group(1);
                }
            }

            // Fallback to element text
            String orderText = driver.findElement(getOrderNumber()).getText();
            System.out.println("Order Element Text: " + orderText);
            return orderText;
        } catch (RuntimeException err) {
            System.out.println("Unable to extract order number: " + err.getMessage());
            return "Order number not found";
        }
    }

    public boolean isOrderConfirmed() {
        try {
            WebElement thankYouMsg = driver.findElement(getThankYouMessage());
            boolean isVisible = thankYouMsg.isDisplayed();
            String text = thankYouMsg.getText().toLowerCase();
            return isVisible && (text.contains("thank you") || text.contains("confirmed"));
        } catch (RuntimeException err) {
            return false;
        }
    }

    public void clickContinueShopping() {
        try {
            WebElement continueBtn = driver.findElement(getContinueShoppingButton());
            if (continueBtn.isDisplayed()) {
                continueBtn.click();
            }
        } catch (RuntimeException err) {
            // Button might not exist
        }
    }

    public boolean validateCustomerNameOnConfirmation(String firstName, String lastName) {
        try {
            String pageText = Helpers.bodyText(driver);
            boolean hasFirstName = pageText.contains(firstName);
            boolean hasLastName = pageText.contains(lastName);

            System.out.println("First Name '" + firstName + "' visible: " + hasFirstName);
            System.out.println("Last Name '" + lastName + "' visible: " + hasLastName);

            return hasFirstName && hasLastName;
        } catch (RuntimeException err) {
            System.out.println("Error validating customer names: " + err.getMessage());
            return false;
        }
    }

    public boolean validateEmailConfirmationMessage() {
        try {
            String pageText = Helpers.bodyText(driver);
            String lower = pageText.toLowerCase();
            boolean hasEmailMessage = lower.contains("confirmation email") || lower.contains("you'll receive");

            System.out.println("Email confirmation message visible: " + hasEmailMessage);
            if (hasEmailMessage) {
                Matcher emailMatch = Pattern
                        .compile("you['’]ll\\s+receive[^.!?]*email[^.!?]*[.!?]", Pattern.CASE_INSENSITIVE)
                        .matcher(pageText);
                if (emailMatch.find()) {
                    System.out.println("Email message: " + emailMatch.group());
                }
            }

            return hasEmailMessage;
        } catch (RuntimeException err) {
            System.out.println("Error validating email confirmation message: " + err.getMessage());
            return false;
        }
    }

    public String getStoredOrderNumber() {
        return storedOrderNumber == null ? "Order number not stored" : storedOrderNumber;
    }
}
