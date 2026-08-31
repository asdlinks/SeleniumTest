import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DealsAndOffersPage {

    private static final Pattern PRICE_PATTERN = Pattern.compile("\\$\\d+(?:\\.\\d{2})?");

    private final WebDriver driver;

    public DealsAndOffersPage(WebDriver driver) {
        this.driver = driver;
    }

    // Locators
    public By getHardwareCategoryButton() {
        return By.xpath("//a[contains(text(), 'Hardware')] | //span[contains(text(), 'Hardware')]"
                + " | //button[contains(text(), 'Hardware')]");
    }

    public By getCategoryLink(String categoryName) {
        return By.xpath("//a[contains(text(), '" + categoryName + "')]"
                + " | //span[contains(text(), '" + categoryName + "')]");
    }

    public By getPageTitle() {
        return By.xpath("//h1[contains(text(), 'Deals')] | //h1[contains(text(), 'Offers')]");
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

    // Actions
    public void selectCategory(String categoryName) {
        WebElement element = Helpers.wait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(getCategoryLink(categoryName)));
        Helpers.scrollToElement(driver, element);
        Helpers.sleep(1000);
        element.click();
    }

    public void waitForPageLoad() {
        Helpers.wait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(getPageTitle()));
    }

    public boolean isDealsOffersPageLoaded() {
        return Helpers.isVisible(driver, getPageTitle());
    }

    public void clickAllProductsLink() {
        WebElement link = Helpers.wait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(getAllProductsLink()));
        Helpers.scrollToElementInstantly(driver, link);
        Helpers.sleep(500);
        Helpers.script(driver, "arguments[0].click();", link);
    }

    public int getProductCardCount() {
        return driver.findElements(getProductCards()).size();
    }

    public void clickLoadMore() {
        WebElement button = Helpers.wait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(getLoadMoreButton()));
        Helpers.scrollToElementInstantly(driver, button);
        Helpers.sleep(500);
        Helpers.script(driver, "arguments[0].click();", button);
        Helpers.sleep(1500);
    }

    public void setPriceRange(int maxValue, int minValue) {
        WebElement minSlider = driver.findElement(getPriceSlider("Minimum price"));
        WebElement maxSlider = driver.findElement(getPriceSlider("Maximum price"));

        moveSliderToValue(minSlider, minValue);
        moveSliderToValue(maxSlider, maxValue);
    }

    public void setPriceRange(int maxValue) {
        setPriceRange(maxValue, 0);
    }

    private void moveSliderToValue(WebElement slider, int targetValue) {
        Helpers.script(driver, "arguments[0].focus();", slider);
        Helpers.sleep(200);

        int currentValue = Integer.parseInt(slider.getAttribute("aria-valuenow"));
        int min = Integer.parseInt(slider.getAttribute("aria-valuemin"));
        int max = Integer.parseInt(slider.getAttribute("aria-valuemax"));
        int safeTarget = Math.min(Math.max(targetValue, min), max);

        while (currentValue < safeTarget) {
            slider.sendKeys(Keys.ARROW_RIGHT);
            currentValue += 1;
        }

        while (currentValue > safeTarget) {
            slider.sendKeys(Keys.ARROW_LEFT);
            currentValue -= 1;
        }

        Helpers.sleep(500);
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
