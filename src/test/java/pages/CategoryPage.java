package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CategoryPage extends BasePage {

    private static final By CATEGORY_TITLE = By.cssSelector("[data-testid='category-title']");

    public CategoryPage(WebDriver driver) {
        super(driver);
    }

    private By productByName(String productName) {
        return By.cssSelector("[data-testid='product-link'][data-product-name='" + productName + "']");
    }

    public boolean isCategoryPageLoaded() {
        try {
            return driver.findElement(CATEGORY_TITLE).isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    public void selectProduct(String productName) {
        WebElement element = waitFor(productByName(productName));
        scrollIntoView(element);
        element.click();
    }
}
