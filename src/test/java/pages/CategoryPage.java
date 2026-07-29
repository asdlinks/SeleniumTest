package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CategoryPage extends BasePage {

    private static final By CATEGORY_TITLE =
            By.xpath("//h1[contains(text(), 'Hardware')] | //h2[contains(text(), 'Hardware')]");

    public CategoryPage(WebDriver driver) {
        super(driver);
    }

    private By productByName(String productName) {
        return By.xpath("//a[contains(text(), '" + productName + "')] | //div[contains(text(), '"
                + productName + "')] | //span[contains(text(), '" + productName + "')]");
    }

    private By productLink(String productName) {
        return By.xpath("//a[contains(@href, '/product')] | //a[contains(text(), '" + productName + "')]");
    }

    public boolean isCategoryPageLoaded() {
        try {
            return driver.findElement(CATEGORY_TITLE).isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    public void selectProduct(String productName) {
        try {
            WebElement element = waitFor(productByName(productName));
            scrollIntoView(element);
            element.click();
        } catch (org.openqa.selenium.TimeoutException e) {
            WebElement link = waitFor(productLink(productName));
            link.click();
        }
    }
}
