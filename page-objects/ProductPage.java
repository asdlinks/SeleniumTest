import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

public class ProductPage {

    private static final Duration DEFAULT_ASSISTANT_TIMEOUT = Duration.ofSeconds(30);

    private final WebDriver driver;

    public ProductPage(WebDriver driver) {
        this.driver = driver;
    }

    // Locators
    public By getProductTitle() {
        return By.xpath("//h1 | //h2[contains(@class, 'product-title')]");
    }

    public By getAddToCartButton() {
        return By.xpath("//span[text()='Add to Cart'] | //button[contains(text(), 'Add to Cart')]"
                + " | //button[contains(text(), 'ADD TO CART')]");
    }

    public By getPrice() {
        return By.xpath("//span[contains(@class, 'price')] | //div[contains(text(), '$')]");
    }

    public By getProductDescription() {
        return By.xpath("//div[contains(@class, 'description')] | //p[contains(@class, 'description')]");
    }

    public By getAddToCartSuccessMessage() {
        return By.xpath("//div[contains(text(), 'added')] | //span[contains(text(), 'Cart')]"
                + " | //div[contains(@class, 'success')]");
    }

    // The "Bricks AI Assistant" widget is the last thing the product page renders
    // (roughly 10 seconds after load), so it is a reliable signal that the page
    // has finished hydrating and "Add to Cart" is safe to click.
    public By getAiAssistantButton() {
        return By.cssSelector("[data-hook=\"minimizedChatButton\"]");
    }

    /**
     * Waits for the Bricks AI Assistant widget to appear before interacting with
     * the page. The widget is a third party add-on, so a timeout is logged and
     * tolerated rather than failing the scenario.
     *
     * @param timeout how long to wait for the widget
     * @return true when the widget became visible
     */
    public boolean waitForAiAssistantReady(Duration timeout) {
        Duration effective = timeout == null ? DEFAULT_ASSISTANT_TIMEOUT : timeout;

        try {
            Helpers.wait(driver, effective)
                    .until(ExpectedConditions.visibilityOfElementLocated(getAiAssistantButton()));
            System.out.println("Bricks AI Assistant is visible - product page has finished loading");
            return true;
        } catch (RuntimeException err) {
            System.out.println("Bricks AI Assistant did not appear within " + effective.toMillis()
                    + "ms, continuing anyway");
            return false;
        }
    }

    // Actions
    public void addToCart() {
        addToCart(null);
    }

    public void addToCart(Duration waitForAiAssistant) {
        // Let the product page finish loading before clicking, otherwise the click
        // lands before the cart is wired up and the item is never added.
        waitForAiAssistantReady(waitForAiAssistant);

        WebElement addToCartBtn = Helpers.wait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(getAddToCartButton()));
        Helpers.scrollToElement(driver, addToCartBtn);
        Helpers.sleep(2000);
        addToCartBtn.click();
    }

    public void waitForProductPageLoad() {
        Helpers.wait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(getProductTitle()));
    }

    public boolean isAddToCartVisible() {
        return Helpers.isVisible(driver, getAddToCartButton());
    }

    public String getProductName() {
        return driver.findElement(getProductTitle()).getText();
    }

    public String getProductPrice() {
        try {
            return driver.findElement(getPrice()).getText();
        } catch (RuntimeException err) {
            return "Price not found";
        }
    }

    // -------------------------------------------------------------------------
    // Data driven validation helpers.
    // These read the product detail page through its stable data-hook attributes
    // so the values can be compared against shared-objects/shopbricks-data.json.
    // -------------------------------------------------------------------------

    public By getProductTitleHook() {
        return By.cssSelector("[data-hook=\"product-title\"]");
    }

    public By getPrimaryPriceHook() {
        return By.cssSelector("[data-hook=\"formatted-primary-price\"]");
    }

    public By getSkuHook() {
        return By.cssSelector("[data-hook=\"sku\"]");
    }

    public By getAddToCartHook() {
        return By.cssSelector("[data-hook=\"add-to-cart\"]");
    }

    public void navigateTo(String url) {
        driver.get(url);
    }

    public void waitForProductDetailsLoad() {
        Helpers.wait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.visibilityOfElementLocated(getProductTitleHook()));
    }

    public String getPageTitle() {
        return driver.getTitle().trim();
    }

    public String getDisplayedName() {
        WebElement title = Helpers.wait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.presenceOfElementLocated(getProductTitleHook()));
        return title.getText().trim();
    }

    public String getDisplayedPriceText() {
        WebElement price = Helpers.wait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.presenceOfElementLocated(getPrimaryPriceHook()));
        return price.getText().trim();
    }

    public double getDisplayedPrice() {
        return Double.parseDouble(getDisplayedPriceText().replaceAll("[^0-9.]", ""));
    }

    public String getDisplayedSku() {
        WebElement sku = Helpers.wait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.presenceOfElementLocated(getSkuHook()));
        return sku.getText().trim().replaceFirst("(?i)^SKU:\\s*", "").trim();
    }

    public boolean isAddToCartButtonPresent() {
        try {
            return driver.findElement(getAddToCartHook()).isDisplayed();
        } catch (RuntimeException err) {
            return isAddToCartVisible();
        }
    }
}
