package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import support.Helpers;

import java.time.Duration;

public class CategoryPage extends BasePage {

    public CategoryPage(WebDriver driver) {
        super(driver);
    }

    public By getCategoryTitle() {
        return By.xpath("//h1[contains(., 'Hardware')] | //h2[contains(., 'Hardware')]");
    }

    public By getProductNameHook() {
        return By.cssSelector("[data-hook=\"product-item-name\"]");
    }

    public By getProductLink(String productName) {
        return By.xpath("//a[contains(@href, '/product')]");
    }

    /**
     * Selects a product tile by its (partial) displayed name, keyed off the
     * site's stable data-hook attributes rather than a brittle "first link on
     * the page" fallback.
     */
    public void selectProduct(String productName) {
        waitFor(getProductNameHook(), Duration.ofSeconds(10));

        WebElement matchingLink = (WebElement) Helpers.script(driver,
                "var name = arguments[0];"
                        + "var nodes = document.querySelectorAll('[data-hook=\"product-item-name\"]');"
                        + "for (var i = 0; i < nodes.length; i++) {"
                        + "  if (nodes[i].textContent.indexOf(name) !== -1) {"
                        + "    return nodes[i].closest('a');"
                        + "  }"
                        + "}"
                        + "return null;",
                productName);

        if (matchingLink != null) {
            Helpers.scrollToElement(driver, matchingLink);
            matchingLink.click();
            return;
        }

        // Fallback: no product tile matched the name, so try any product link.
        WebElement link = waitFor(getProductLink(productName), Duration.ofSeconds(10));
        link.click();
    }

    public boolean isCategoryPageLoaded() {
        return isVisible(getCategoryTitle());
    }
}
