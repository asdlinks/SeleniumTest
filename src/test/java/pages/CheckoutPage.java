package pages;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class CheckoutPage extends BasePage {

    private static final By PLACE_ORDER_BUTTON = By.xpath(
            "//button[contains(translate(., 'PLACE ORDER', 'place order'), 'place order')] | "
                    + "//button[contains(translate(., 'CONTINUE', 'continue'), 'continue')]");

    public CheckoutPage(WebDriver driver) {
        super(driver);
    }

    private List<By> fieldLocators(String fieldKey) {
        switch (fieldKey) {
            case "email":
                return List.of(By.name("email"), By.cssSelector("input[type='email']"));
            case "firstName":
                return List.of(By.name("firstName"), By.name("first_name"), By.name("firstname"));
            case "lastName":
                return List.of(By.name("lastName"), By.name("last_name"), By.name("lastname"));
            case "address1":
                return List.of(By.name("address1"), By.name("address"));
            case "city":
                return List.of(By.name("city"));
            case "zip":
                return List.of(By.name("zip"), By.name("postalCode"), By.name("postal_code"));
            case "phone":
                return List.of(By.name("phone"), By.name("mobile"));
            case "country":
                return List.of(By.name("country"), By.cssSelector("select[name='country']"));
            case "state":
                return List.of(By.name("state"), By.cssSelector("select[name='State']"));
            default:
                return List.of(By.name(fieldKey));
        }
    }

    private WebElement findFieldElement(String fieldKey) {
        for (By locator : fieldLocators(fieldKey)) {
            try {
                WebElement element = driver.findElement(locator);
                if (element.isDisplayed()) {
                    return element;
                }
            } catch (NoSuchElementException ignored) {
                // try next locator
            }
        }
        throw new NoSuchElementException("Field '" + fieldKey + "' not found");
    }

    private void selectDropdownValue(String fieldKey, String value) {
        WebElement dropdown = findFieldElement(fieldKey);
        String tagName = dropdown.getTagName();

        if ("select".equalsIgnoreCase(tagName)) {
            Select select = new Select(dropdown);
            select.selectByVisibleText(value);
            return;
        }

        dropdown.click();
        dropdown.sendKeys(value);
        dropdown.sendKeys(Keys.ARROW_DOWN);
        dropdown.sendKeys(Keys.ENTER);
    }

    public void fillCustomerDetails(Map<String, String> details) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("email", details.get("email"));
        fields.put("firstName", details.get("firstName"));
        fields.put("lastName", details.get("lastName"));
        fields.put("phone", details.get("phone"));
        fields.put("country", details.get("country"));
        fields.put("address1", details.get("address1"));
        fields.put("city", details.get("city"));
        fields.put("state", details.get("region"));
        fields.put("zip", details.get("zip"));

        for (Map.Entry<String, String> field : fields.entrySet()) {
            String value = field.getValue();
            if (value == null || value.isEmpty()) {
                continue;
            }

            try {
                if ("country".equals(field.getKey()) || "state".equals(field.getKey())) {
                    selectDropdownValue(field.getKey(), value);
                    continue;
                }

                WebElement element = findFieldElement(field.getKey());
                scrollIntoView(element);
                element.clear();
                element.sendKeys(value);
            } catch (NoSuchElementException ignored) {
                // field not present on this checkout form; skip
            }
        }
    }

    public void placeOrder() {
        WebElement placeOrderBtn = waitForClickable(PLACE_ORDER_BUTTON);
        scrollIntoView(placeOrderBtn);
        placeOrderBtn.click();
    }

    public void waitForPageTransition() {
        wait.withTimeout(Duration.ofSeconds(10)).until(webDriver ->
                "complete".equals(((JavascriptExecutor) webDriver).executeScript("return document.readyState")));
    }
}
