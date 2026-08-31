package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import support.Helpers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class HomePage extends BasePage {

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public By getDealsOffersLink() {
        return By.xpath("//a[contains(., 'Deals') or contains(., 'Offers')]");
    }

    public By getNavDealsOffersButton() {
        return By.xpath("//span[contains(., 'Deals')] | //span[contains(., 'Offers')]");
    }

    public By getShopByCategoryHeading() {
        return By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(.), 'Shop By Category')]");
    }

    public By getTrustedBrandsHeading() {
        return By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(.), 'Our Trusted Brands')]");
    }

    public By getSectionItemLocator(String itemText) {
        return By.xpath("//a[contains(normalize-space(.), '" + itemText + "')]"
                + " | //button[contains(normalize-space(.), '" + itemText + "')]"
                + " | //span[contains(normalize-space(.), '" + itemText + "')]"
                + " | //div[contains(normalize-space(.), '" + itemText + "')]");
    }

    public void navigateToHome(String url) {
        driver.get(url);
    }

    public void waitForPageLoad() {
        Helpers.waitForDocumentReady(driver, Duration.ofSeconds(15));
    }

    public void clickDealsAndOffers() {
        try {
            waitFor(getDealsOffersLink()).click();
        } catch (RuntimeException err) {
            waitFor(getNavDealsOffersButton()).click();
        }
    }

    public String getTextFromLocator(By locator) {
        return waitFor(locator, Duration.ofSeconds(10)).getText().trim();
    }

    public boolean isLocatorVisible(By locator) {
        return isVisible(locator);
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

    public WebElement locate(By locator) {
        return driver.findElement(locator);
    }
}
