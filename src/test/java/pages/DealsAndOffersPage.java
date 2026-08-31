package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import support.Helpers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DealsAndOffersPage extends BasePage {

    private static final Pattern PRICE_PATTERN = Pattern.compile("\\$\\d+(?:\\.\\d{2})?");

    public DealsAndOffersPage(WebDriver driver) {
        super(driver);
    }

    public By getCategoryLink(String categoryName) {
        return By.xpath("//a[contains(., '" + categoryName + "')]"
                + " | //span[contains(., '" + categoryName + "')]");
    }

    public By getPageTitle() {
        return By.xpath("//h1[contains(., 'Deals')] | //h1[contains(., 'Offers')]");
    }

    public By getAllProductsLink() {
        return By.xpath("//aside//a[contains(normalize-space(.), 'All Products')]");
    }

    public By getLoadMoreButton() {
        return By.xpath("//button[contains(normalize-space(.), 'Load More')]");
    }

    public By getPriceSlider(String label) {
        return By.cssSelector("[role=\"slider\"][aria-label=\"" + label + "\"]");
    }

    public By getProductCards() {
        return By.cssSelector("main a[href*=\"/product-page/\"]");
    }

    public void selectCategory(String categoryName) {
        WebElement element = waitFor(getCategoryLink(categoryName), Duration.ofSeconds(10));
        Helpers.scrollToElement(driver, element);
        element.click();
    }

    public void waitForPageLoad() {
        waitFor(getPageTitle(), Duration.ofSeconds(10));
    }

    public boolean isDealsOffersPageLoaded() {
        return isVisible(getPageTitle());
    }

    public void clickAllProductsLink() {
        WebElement link = waitFor(getAllProductsLink(), Duration.ofSeconds(10));
        Helpers.scrollToElementInstantly(driver, link);
        Helpers.script(driver, "arguments[0].click();", link);
    }

    public int getProductCardCount() {
        return driver.findElements(getProductCards()).size();
    }

    public void clickLoadMore() {
        WebElement button = waitFor(getLoadMoreButton(), Duration.ofSeconds(10));
        Helpers.scrollToElementInstantly(driver, button);
        Helpers.script(driver, "arguments[0].click();", button);
    }

    public void setPriceRange(int maxValue, int minValue) {
        WebElement minSlider = driver.findElement(getPriceSlider("Minimum price"));
        WebElement maxSlider = driver.findElement(getPriceSlider("Maximum price"));

        moveSliderToValue(minSlider, minValue);
        moveSliderToValue(maxSlider, maxValue);
    }

    private void moveSliderToValue(WebElement slider, int targetValue) {
        Helpers.script(driver, "arguments[0].focus();", slider);

        int min = Integer.parseInt(slider.getAttribute("aria-valuemin"));
        int max = Integer.parseInt(slider.getAttribute("aria-valuemax"));
        int safeTarget = Math.min(Math.max(targetValue, min), max);

        // Step one key press at a time, waiting for the slider's own value to
        // update before pressing again - sending the whole run of key presses at
        // once outruns the widget's (debounced) value updates and overshoots.
        Keys direction = Integer.parseInt(slider.getAttribute("aria-valuenow")) < safeTarget
                ? Keys.ARROW_RIGHT : Keys.ARROW_LEFT;

        int currentValue = Integer.parseInt(slider.getAttribute("aria-valuenow"));
        while (currentValue != safeTarget) {
            String before = slider.getAttribute("aria-valuenow");
            slider.sendKeys(direction);
            try {
                Helpers.wait(driver, Duration.ofSeconds(10))
                        .until(d -> !before.equals(slider.getAttribute("aria-valuenow")));
            } catch (org.openqa.selenium.TimeoutException timeout) {
                // The slider stopped responding to further key presses (likely
                // hit a boundary imposed by the other handle) - stop here
                // rather than looping forever.
                break;
            }
            currentValue = Integer.parseInt(slider.getAttribute("aria-valuenow"));

            if (direction == Keys.ARROW_RIGHT && currentValue > safeTarget) {
                break;
            }
            if (direction == Keys.ARROW_LEFT && currentValue < safeTarget) {
                break;
            }
        }
    }

    public List<String> getProductPrices() {
        String bodyText = driver.findElement(By.cssSelector("body")).getText();
        List<String> prices = new ArrayList<>();

        Matcher matcher = PRICE_PATTERN.matcher(bodyText);
        while (matcher.find()) {
            prices.add(matcher.group());
        }

        return prices;
    }

    public String getFilterRangeText() {
        return Helpers.bodyText(driver);
    }
}
