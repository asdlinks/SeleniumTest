package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import support.Helpers;

import java.time.Duration;

public class ProductPage extends BasePage {

    private static final Duration DEFAULT_ASSISTANT_TIMEOUT = Duration.ofSeconds(30);

    public ProductPage(WebDriver driver) {
        super(driver);
    }

    public By getProductTitle() {
        return By.xpath("//h1 | //h2[contains(@class, 'product-title')]");
    }

    public By getAddToCartButton() {
        return By.xpath("//span[normalize-space(.)='Add to Cart'] | //button[contains(., 'Add to Cart')]"
                + " | //button[contains(., 'ADD TO CART')]");
    }

    // The "Bricks AI Assistant" widget is the last thing the product page renders
    // (roughly 10 seconds after load), so it is a reliable signal that the page
    // has finished hydrating and "Add to Cart" is safe to click.
    public By getAiAssistantButton() {
        return By.cssSelector("[data-hook=\"minimizedChatButton\"]");
    }

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

    /**
     * Waits for the Bricks AI Assistant widget to appear before interacting with
     * the page. The widget is a third party add-on, so a timeout is logged and
     * tolerated rather than failing the scenario.
     */
    public boolean waitForAiAssistantReady(Duration timeout) {
        Duration effective = timeout == null ? DEFAULT_ASSISTANT_TIMEOUT : timeout;

        try {
            waitFor(getAiAssistantButton(), effective);
            return true;
        } catch (RuntimeException err) {
            System.out.println("Bricks AI Assistant did not appear within " + effective.toMillis()
                    + "ms, continuing anyway");
            return false;
        }
    }

    public void addToCart(Duration waitForAiAssistant) {
        waitForAiAssistantReady(waitForAiAssistant);

        WebElement addToCartBtn = waitFor(getAddToCartButton(), Duration.ofSeconds(10));
        Helpers.scrollToElement(driver, addToCartBtn);
        addToCartBtn.click();
    }

    public boolean isAddToCartVisible() {
        return isVisible(getAddToCartButton());
    }

    public void navigateTo(String url) {
        driver.get(url);
    }

    public void waitForProductDetailsLoad() {
        waitFor(getProductTitleHook(), Duration.ofSeconds(20));
    }

    public String getPageTitle() {
        return driver.getTitle().trim();
    }

    public String getDisplayedName() {
        return waitFor(getProductTitleHook(), Duration.ofSeconds(20)).getText().trim();
    }

    public String getDisplayedPriceText() {
        return waitFor(getPrimaryPriceHook(), Duration.ofSeconds(20)).getText().trim();
    }

    public double getDisplayedPrice() {
        return Double.parseDouble(getDisplayedPriceText().replaceAll("[^0-9.]", ""));
    }

    public String getDisplayedSku() {
        String text = waitFor(getSkuHook(), Duration.ofSeconds(20)).getText().trim();
        return text.replaceFirst("(?i)^SKU:\\s*", "").trim();
    }

    public boolean isAddToCartButtonPresent() {
        try {
            return driver.findElement(getAddToCartHook()).isDisplayed();
        } catch (RuntimeException err) {
            return isAddToCartVisible();
        }
    }
}
