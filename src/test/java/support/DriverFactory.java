package support;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

/**
 * Creates one headless ChromeDriver per test via Selenium 4's Selenium
 * Manager. Stashes the live driver in a ThreadLocal so it can be reached
 * from anywhere in the current test thread (e.g. an injected recording
 * extension), without every test class wiring its own accessor.
 */
public final class DriverFactory {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverFactory() {
    }

    public static WebDriver create() {
        ChromeOptions options = new ChromeOptions();
        if (!MaestroRecorder.headful()) options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1280,720");

        WebDriver driver = MaestroEvents.decorate(new ChromeDriver(options));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));

        DRIVER.set(driver);
        return driver;
    }

    public static WebDriver current() {
        return DRIVER.get();
    }

    public static void quit() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
        }
    }
}
