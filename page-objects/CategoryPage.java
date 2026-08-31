import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

public class CategoryPage {

    private final WebDriver driver;

    public CategoryPage(WebDriver driver) {
        this.driver = driver;
    }

    // Locators
    public By getCategoryTitle() {
        return By.xpath("//h1[contains(text(), 'Hardware')] | //h2[contains(text(), 'Hardware')]");
    }

    public By getProductByName(String productName) {
        return By.xpath("//a[contains(text(), '" + productName + "')]"
                + " | //div[contains(text(), '" + productName + "')]"
                + " | //span[contains(text(), '" + productName + "')]");
    }

    public By getProductLink(String productName) {
        return By.xpath("//a[contains(@href, '/product')] | //a[contains(text(), '" + productName + "')]");
    }

    public By getFirstProduct() {
        return By.cssSelector("a[href*=\"/product\"], .product-card a, .product-item a");
    }

    // Actions
    public void selectProduct(String productName) {
        try {
            WebElement element = Helpers.wait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(getProductByName(productName)));
            Helpers.scrollToElement(driver, element);
            Helpers.sleep(500);
            element.click();
        } catch (RuntimeException err) {
            // If product name not found, try to find a link that leads to the product
            WebElement link = Helpers.wait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(getProductLink(productName)));
            link.click();
        }
    }

    public void waitForCategoryPageLoad() {
        Helpers.wait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(getCategoryTitle()));
    }

    public boolean isCategoryPageLoaded() {
        return Helpers.isVisible(driver, getCategoryTitle());
    }
}
