package support;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Shared browser utilities used by the page objects, mirroring the original
 * suite's runtime/Helpers.java (scroll/script/visibility helpers on top of
 * explicit WebDriverWait synchronization).
 */
public final class Helpers {

    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);

    private Helpers() {
    }

    public static WebDriverWait wait(WebDriver driver, Duration timeout) {
        return new WebDriverWait(driver, timeout);
    }

    public static WebDriverWait wait(WebDriver driver) {
        return wait(driver, DEFAULT_TIMEOUT);
    }

    public static void waitForDocumentReady(WebDriver driver, Duration timeout) {
        wait(driver, timeout).until(d -> "complete".equals(script(d, "return document.readyState")));
    }

    public static void scrollToElement(WebDriver driver, WebElement element) {
        script(driver, "arguments[0].scrollIntoView({ behavior: 'smooth', block: 'center' });", element);
    }

    public static void scrollToElementInstantly(WebDriver driver, WebElement element) {
        script(driver, "arguments[0].scrollIntoView({ block: 'center' });", element);
    }

    public static Object script(WebDriver driver, String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    public static String bodyText(WebDriver driver) {
        Object text = script(driver, "return document.body.innerText");
        return text == null ? "" : text.toString();
    }

    public static boolean isVisible(WebDriver driver, By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (RuntimeException e) {
            return false;
        }
    }
}
