package pages;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class DealsAndOffersPage extends BasePage {

    private static final By PAGE_TITLE =
            By.xpath("//h1[contains(text(), 'Deals')] | //h1[contains(text(), 'Offers')]");
    private static final By ALL_PRODUCTS_LINK =
            By.xpath("//aside//a[contains(normalize-space(.), 'All Products')]");
    private static final By LOAD_MORE_BUTTON =
            By.xpath("//button[contains(normalize-space(.), 'Load More')]");
    private static final By PRODUCT_CARDS = By.cssSelector("main a[href*='/product-page/']");

    public DealsAndOffersPage(WebDriver driver) {
        super(driver);
    }

    private By categoryLink(String categoryName) {
        return By.xpath("//a[contains(text(), '" + categoryName + "')] | //span[contains(text(), '" + categoryName + "')]");
    }

    private By priceSlider(String label) {
        return By.cssSelector("[role='slider'][aria-label='" + label + "']");
    }

    public void waitForPageLoad() {
        waitFor(PAGE_TITLE);
    }

    public boolean isDealsOffersPageLoaded() {
        try {
            return driver.findElement(PAGE_TITLE).isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    public void selectCategory(String categoryName) {
        WebElement element = waitFor(categoryLink(categoryName));
        scrollIntoView(element);
        element.click();
    }

    public void clickAllProductsLink() {
        WebElement link = waitFor(ALL_PRODUCTS_LINK);
        scrollIntoView(link);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
    }

    public int getProductCardCount() {
        return driver.findElements(PRODUCT_CARDS).size();
    }

    public void clickLoadMore() {
        WebElement button = waitFor(LOAD_MORE_BUTTON);
        scrollIntoView(button);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
    }

    public void setPriceRange(int maxValue, int minValue) {
        WebElement minSlider = driver.findElement(priceSlider("Minimum price"));
        WebElement maxSlider = driver.findElement(priceSlider("Maximum price"));

        moveSliderToValue(minSlider, minValue);
        moveSliderToValue(maxSlider, maxValue);
    }

    private void moveSliderToValue(WebElement slider, int targetValue) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].focus();", slider);

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
    }

    public List<String> getProductPrices() {
        String bodyText = driver.findElement(By.cssSelector("body")).getText();
        List<String> prices = new java.util.ArrayList<>();
        Matcher matcher = Pattern.compile("\\$\\d+(?:\\.\\d{2})?").matcher(bodyText);
        while (matcher.find()) {
            prices.add(matcher.group());
        }
        return prices;
    }

    public String getFilterRangeText() {
        return (String) ((JavascriptExecutor) driver).executeScript("return document.body.innerText;");
    }
}
