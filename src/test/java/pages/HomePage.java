package pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class HomePage extends BasePage {

    private static final By DEALS_OFFERS_LINK = By.cssSelector("[data-testid='deals-offers-link']");
    private static final By NAV_DEALS_OFFERS_BUTTON = By.cssSelector("[data-testid='nav-deals-offers-button']");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public void navigateTo(String url) {
        driver.get(url);
    }

    public void waitForPageLoad() {
        wait.withTimeout(Duration.ofSeconds(15)).until(webDriver ->
                "complete".equals(((JavascriptExecutor) webDriver).executeScript("return document.readyState")));
    }

    public void clickDealsAndOffers() {
        try {
            waitForClickable(DEALS_OFFERS_LINK).click();
        } catch (org.openqa.selenium.TimeoutException e) {
            waitForClickable(NAV_DEALS_OFFERS_BUTTON).click();
        }
    }
}
