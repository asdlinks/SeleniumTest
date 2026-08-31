import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared browser utilities, the Java port of runtime/helpers.js.
 *
 * The page objects use these instead of repeating wait/scroll boilerplate.
 */
public final class Helpers {

    /** Default wait used across the framework, matching DEFAULT_TIMEOUT in index.js. */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);

    private Helpers() {
    }

    public static WebDriverWait wait(WebDriver driver, Duration timeout) {
        return new WebDriverWait(driver, timeout);
    }

    public static WebDriverWait wait(WebDriver driver) {
        return wait(driver, DEFAULT_TIMEOUT);
    }

    /** loadPage(url, waitInSeconds) */
    public static void loadPage(WebDriver driver, String url, Duration timeout) {
        driver.get(url);
        waitForDocumentReady(driver, timeout);
    }

    /** Waits for document.readyState to become 'complete'. */
    public static void waitForDocumentReady(WebDriver driver, Duration timeout) {
        wait(driver, timeout).until(d -> "complete".equals(script(d, "return document.readyState")));
    }

    /** getAttributeValue(htmlCssSelector, attributeName) */
    public static String getAttributeValue(WebDriver driver, String cssSelector, String attributeName) {
        return driver.findElement(By.cssSelector(cssSelector)).getAttribute(attributeName);
    }

    /** getElementsContainingText(cssSelector, textToMatch) */
    public static List<WebElement> getElementsContainingText(WebDriver driver, String cssSelector, String textToMatch) {
        List<WebElement> matches = new ArrayList<>();

        for (WebElement element : driver.findElements(By.cssSelector(cssSelector))) {
            String text = element.getText();
            if (text != null && text.contains(textToMatch)) {
                matches.add(element);
            }
        }

        return matches;
    }

    /** getFirstElementContainingText(cssSelector, textToMatch) */
    public static WebElement getFirstElementContainingText(WebDriver driver, String cssSelector, String textToMatch) {
        List<WebElement> matches = getElementsContainingText(driver, cssSelector, textToMatch);
        return matches.isEmpty() ? null : matches.get(0);
    }

    /** clickHiddenElement(cssSelector, textToMatch) */
    public static void clickHiddenElement(WebDriver driver, String cssSelector, String textToMatch) {
        script(driver,
                "var elements = document.querySelectorAll(arguments[0]);"
                        + "for (var i = 0; i < elements.length; i++) {"
                        + "  if (!arguments[1] || elements[i].textContent.indexOf(arguments[1]) !== -1) {"
                        + "    elements[i].click();"
                        + "    return true;"
                        + "  }"
                        + "}"
                        + "return false;",
                cssSelector, textToMatch);
    }

    /** waitUntilAttributeEquals(elementSelector, attributeName, attributeValue, waitInMilliseconds) */
    public static void waitUntilAttributeEquals(WebDriver driver, By locator, String attributeName,
                                                String attributeValue, Duration timeout) {
        wait(driver, timeout).until(ExpectedConditions.attributeToBe(locator, attributeName, attributeValue));
    }

    /** waitUntilAttributeExists(elementSelector, attributeName, waitInMilliseconds) */
    public static void waitUntilAttributeExists(WebDriver driver, By locator, String attributeName, Duration timeout) {
        wait(driver, timeout).until(d -> d.findElement(locator).getAttribute(attributeName) != null);
    }

    /** waitUntilAttributeDoesNotExists(elementSelector, attributeName, waitInMilliseconds) */
    public static void waitUntilAttributeDoesNotExist(WebDriver driver, By locator, String attributeName,
                                                      Duration timeout) {
        wait(driver, timeout).until(d -> d.findElement(locator).getAttribute(attributeName) == null);
    }

    /** waitForCssXpathElement(elementSelector, waitInMilliseconds) */
    public static WebElement waitForElement(WebDriver driver, By locator, Duration timeout) {
        return wait(driver, timeout).until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public static WebElement waitForVisibleElement(WebDriver driver, By locator, Duration timeout) {
        return wait(driver, timeout).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /** scrollToElement(element) */
    public static void scrollToElement(WebDriver driver, WebElement element) {
        script(driver, "arguments[0].scrollIntoView({ behavior: \"smooth\", block: \"center\" });", element);
    }

    public static void scrollToElementInstantly(WebDriver driver, WebElement element) {
        script(driver, "arguments[0].scrollIntoView({ block: \"center\" });", element);
    }

    /** selectDropdownValueByVisibleText(elementSelector, optionName) */
    public static void selectDropdownValueByVisibleText(WebDriver driver, By locator, String optionName) {
        new Select(driver.findElement(locator)).selectByVisibleText(optionName);
    }

    /** waitForNewWindows(waitInMilliseconds) */
    public static void waitForNewWindow(WebDriver driver, int expectedWindowCount, Duration timeout) {
        wait(driver, timeout).until(ExpectedConditions.numberOfWindowsToBe(expectedWindowCount));
    }

    /** getPseudoElementBeforeValue(cssSelector) */
    public static String getPseudoElementBeforeValue(WebDriver driver, String cssSelector) {
        return getPseudoElementValue(driver, cssSelector, ":before");
    }

    /** getPseudoElementAfterValue(cssSelector) */
    public static String getPseudoElementAfterValue(WebDriver driver, String cssSelector) {
        return getPseudoElementValue(driver, cssSelector, ":after");
    }

    private static String getPseudoElementValue(WebDriver driver, String cssSelector, String pseudo) {
        Object value = script(driver,
                "return window.getComputedStyle(document.querySelector(arguments[0]), arguments[1])"
                        + ".getPropertyValue('content');",
                cssSelector, pseudo);
        return value == null ? null : value.toString();
    }

    /** clearCookies() */
    public static void clearCookies(WebDriver driver) {
        driver.manage().deleteAllCookies();
    }

    /** clearStorages() */
    public static void clearStorages(WebDriver driver) {
        script(driver, "try { localStorage.clear(); sessionStorage.clear(); } catch (e) { }");
    }

    /** clearCookiesAndStorages() */
    public static void clearCookiesAndStorages(WebDriver driver) {
        clearCookies(driver);
        clearStorages(driver);
    }

    /** Pauses the current thread, the Java equivalent of driver.sleep(ms). */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while sleeping", e);
        }
    }

    /** Runs JavaScript in the page, the equivalent of driver.executeScript(). */
    public static Object script(WebDriver driver, String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    /** @return document.body.innerText */
    public static String bodyText(WebDriver driver) {
        Object text = script(driver, "return document.body.innerText");
        return text == null ? "" : text.toString();
    }

    /** @return true when the element exists and is displayed, false otherwise */
    public static boolean isVisible(WebDriver driver, By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (RuntimeException e) {
            return false;
        }
    }
}
