import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class HomePage {

    private final WebDriver driver;

    public HomePage(WebDriver driver) {
        this.driver = driver;
    }

    // Locators
    public By getDealsOffersLink() {
        return By.xpath("//a[contains(text(), 'Deals') or contains(text(), 'Offers')]");
    }

    public By getNavDealsOffersButton() {
        return By.xpath("//span[contains(text(), 'Deals')] | //span[contains(text(), 'Offers')]");
    }

    // Common elements
    public By getSiteLogo() {
        return By.cssSelector("img[alt*=\"logo\"], .logo");
    }

    // Actions
    public void navigateToHome(String url) {
        driver.get(url);
    }

    public void clickDealsAndOffers() {
        try {
            driver.findElement(getDealsOffersLink()).click();
        } catch (RuntimeException err) {
            // Try alternative selector
            driver.findElement(getNavDealsOffersButton()).click();
        }
    }

    public By getShopByCategoryHeading() {
        return By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(.), 'Shop By Category')]");
    }

    public By getSectionItemLocator(String itemText) {
        return By.xpath("//a[contains(normalize-space(.), '" + itemText + "')]"
                + " | //button[contains(normalize-space(.), '" + itemText + "')]"
                + " | //span[contains(normalize-space(.), '" + itemText + "')]"
                + " | //div[contains(normalize-space(.), '" + itemText + "')]");
    }

    public By getTrustedBrandsHeading() {
        return By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(.), 'Our Trusted Brands')]");
    }

    @SuppressWarnings("unchecked")
    public List<String> getItemsUnderHeading(String headingText) {
        Object result = Helpers.script(driver,
                "var headingText = arguments[0];"
                        + "var heading = Array.prototype.slice.call(document.querySelectorAll('h1,h2,h3'))"
                        + "  .find(function (el) { return el.textContent.trim().indexOf(headingText) !== -1; });"
                        + "if (!heading) { return []; }"
                        + "var section = heading.closest('section,div,aside') || heading.parentElement;"
                        + "if (!section) { return []; }"
                        + "var itemNodes = Array.prototype.slice.call(section.querySelectorAll('a,button,span,li'));"
                        + "var cleaned = itemNodes"
                        + "  .map(function (el) { return el.textContent.trim(); })"
                        + "  .filter(function (text) { return text && text !== headingText; });"
                        + "return Array.prototype.slice.call(new Set(cleaned));",
                headingText);

        return result == null ? new ArrayList<>() : (List<String>) result;
    }

    public String getTextFromLocator(By locator) {
        WebElement element = Helpers.wait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
        return element.getText().trim();
    }

    public boolean isLocatorVisible(By locator) {
        return Helpers.isVisible(driver, locator);
    }

    public void waitForPageLoad() {
        Helpers.waitForDocumentReady(driver, Duration.ofSeconds(15));
    }
}
