import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

/**
 * Creates the Selenium WebDriver, replacing runtime/chromeDriver.js and
 * runtime/firefoxDriver.js.
 *
 * Selenium 4 ships Selenium Manager, which downloads and matches the browser
 * driver automatically. There is no chromedriver dependency to keep in step
 * with the installed Chrome version.
 *
 * Set -Dbrowser=firefox to switch browsers, -Dheadless=true to run headless.
 */
public final class DriverFactory {

    private static final Duration IMPLICIT_WAIT = Duration.ofSeconds(0);
    private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(60);

    private DriverFactory() {
    }

    /**
     * Builds a driver for the browser named by the `browser` system property.
     *
     * @return a started WebDriver, maximised
     */
    public static WebDriver create() {
        String browser = System.getProperty("browser", "chrome").toLowerCase();
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));

        WebDriver driver;

        switch (browser) {
            case "firefox":
                driver = createFirefox(headless);
                break;
            case "chrome":
                driver = createChrome(headless);
                break;
            default:
                throw new IllegalArgumentException("Unsupported browser '" + browser + "'. Use chrome or firefox.");
        }

        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT);
        driver.manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT);
        driver.manage().window().maximize();

        return driver;
    }

    private static WebDriver createChrome(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized", "--disable-extensions");

        if (headless) {
            options.addArguments("--headless=new", "--window-size=1920,1080");
        }

        return new ChromeDriver(options);
    }

    private static WebDriver createFirefox(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();

        if (headless) {
            options.addArguments("-headless");
        }

        return new FirefoxDriver(options);
    }
}
