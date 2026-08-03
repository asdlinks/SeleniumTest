package tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import support.Config;
import support.DriverFactory;

public class AllProductsCatalogBrowseTest {

    private static final By PRODUCT_CARDS = By.cssSelector("main a[href*='/product-page/']");
    private static final By LOAD_MORE_BUTTON = By.xpath("//button[contains(normalize-space(.), 'Load More')]");
    private static final By SORT_TRIGGER = By.xpath("//*[contains(text(), 'Recommended')]");
    private static final By SELECT_OPTIONS = By.cssSelector("[role='option']");
    private static final Pattern PRICE_PATTERN = Pattern.compile("\\$\\d+(?:\\.\\d{2})?");

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        driver = DriverFactory.create();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void tearDown() {
        DriverFactory.quit();
    }

    private By priceSlider(String label) {
        return By.cssSelector("[role='slider'][aria-label='" + label + "']");
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

    private List<String> extractPrices(String text) {
        List<String> prices = new java.util.ArrayList<>();
        Matcher matcher = PRICE_PATTERN.matcher(text);
        while (matcher.find()) {
            prices.add(matcher.group());
        }
        return prices;
    }

    private List<String> currentProductHrefs() {
        return driver.findElements(PRODUCT_CARDS).stream()
                .map(element -> element.getAttribute("href"))
                .collect(Collectors.toList());
    }

    private String bodyText() {
        return driver.findElement(By.tagName("body")).getText();
    }

    @Test
    void browsesFiltersSortsAndPaginatesAllProductsCatalog() {
        driver.get(Config.baseUrl() + "category/all-products");
        wait.until(ExpectedConditions.visibilityOfElementLocated(PRODUCT_CARDS));

        int initialCount = driver.findElements(PRODUCT_CARDS).size();
        assertTrue(initialCount > 0, "Expected the All Products grid to render at least one product");

        List<String> initialPrices = extractPrices(bodyText());
        assertFalse(initialPrices.isEmpty(), "Expected product prices to be visible on the All Products page");

        WebElement minSlider = driver.findElement(priceSlider("Minimum price"));
        WebElement maxSlider = driver.findElement(priceSlider("Maximum price"));
        moveSliderToValue(minSlider, 0);
        moveSliderToValue(maxSlider, 100);

        wait.until(driverRef -> bodyText().contains("100"));
        String filteredBody = bodyText();
        assertTrue(filteredBody.contains("100"), "Expected the price filter text to reflect the selected max of 100");
        List<String> filteredPrices = extractPrices(filteredBody);
        assertFalse(filteredPrices.isEmpty(), "Expected at least one product to remain after filtering by price");

        List<String> orderedHrefsBeforeSort = currentProductHrefs();
        WebElement sortTrigger = wait.until(ExpectedConditions.elementToBeClickable(SORT_TRIGGER));
        sortTrigger.click();
        List<WebElement> options = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(SELECT_OPTIONS));
        options.stream()
                .filter(option -> !option.getText().trim().equalsIgnoreCase("Recommended"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected an alternative sort option besides Recommended"))
                .click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(PRODUCT_CARDS));
        List<String> orderedHrefsAfterSort = currentProductHrefs();
        assertTrue(!orderedHrefsAfterSort.isEmpty(), "Expected products to remain visible after changing sort order");
        assertFalse(orderedHrefsBeforeSort.equals(orderedHrefsAfterSort),
                "Expected product order to change after selecting a different sort option");

        List<String> hrefsBeforeLoadMore = currentProductHrefs();
        WebElement loadMoreButton = wait.until(ExpectedConditions.visibilityOfElementLocated(LOAD_MORE_BUTTON));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", loadMoreButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loadMoreButton);

        wait.until(driverRef -> driverRef.findElements(PRODUCT_CARDS).size() > hrefsBeforeLoadMore.size());
        List<String> hrefsAfterLoadMore = currentProductHrefs();

        assertTrue(hrefsAfterLoadMore.size() > hrefsBeforeLoadMore.size(),
                "Expected additional products to load after clicking Load More");
        Set<String> uniqueHrefs = new HashSet<>(hrefsAfterLoadMore);
        assertTrue(uniqueHrefs.size() == hrefsAfterLoadMore.size(),
                "Expected no duplicate products between the first and second pages");
        assertTrue(uniqueHrefs.containsAll(hrefsBeforeLoadMore),
                "Expected products from the first page to still be present after loading more");
    }
}
