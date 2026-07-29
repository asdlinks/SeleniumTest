package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ProductPage extends BasePage {

    private static final By ADD_TO_CART_BUTTON = By.xpath(
            "//span[text()='Add to Cart'] | //button[contains(text(), 'Add to Cart')] | //button[contains(text(), 'ADD TO CART')]");

    public ProductPage(WebDriver driver) {
        super(driver);
    }

    public boolean isAddToCartVisible() {
        try {
            return driver.findElement(ADD_TO_CART_BUTTON).isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    public void addToCart() {
        WebElement button = waitFor(ADD_TO_CART_BUTTON);
        scrollIntoView(button);
        button.click();
    }
}
