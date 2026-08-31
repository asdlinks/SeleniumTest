package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import support.Helpers;

import java.time.Duration;

/**
 * Base for every page object: constructor takes the WebDriver, exposes
 * waitFor(By) wrapping WebDriverWait so every subclass gets consistent
 * synchronization without repeating the boilerplate.
 */
public abstract class BasePage {

    protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);

    protected final WebDriver driver;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
    }

    protected WebElement waitFor(By locator) {
        return waitFor(locator, DEFAULT_TIMEOUT);
    }

    protected WebElement waitFor(By locator, Duration timeout) {
        return new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickable(By locator, Duration timeout) {
        return new WebDriverWait(driver, timeout).until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected boolean isVisible(By locator) {
        return Helpers.isVisible(driver, locator);
    }
}
