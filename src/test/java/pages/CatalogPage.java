package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import support.Helpers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generic storefront listing page (any /category/&lt;key&gt; page).
 *
 * Used by the data driven tests to read what the site actually renders so it
 * can be compared against the expected values held in
 * src/test/resources/shopbricks-data.json.
 */
public class CatalogPage extends BasePage {

    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    public CatalogPage(WebDriver driver) {
        super(driver);
    }

    /** One product tile as rendered by the site. */
    public static class ListedProduct {
        public final String name;
        public final String slug;
        public final String priceText;
        public final Double price;

        ListedProduct(String name, String slug, String priceText, Double price) {
            this.name = name;
            this.slug = slug;
            this.priceText = priceText;
            this.price = price;
        }
    }

    /** One entry in the "Browse by" sidebar. */
    public static class SidebarCategory {
        public final String label;
        public final String url;

        SidebarCategory(String label, String url) {
            this.label = label;
            this.url = url;
        }
    }

    /** Bounds the price range slider was rendered with. */
    public static class PriceBounds {
        public final int minimum;
        public final int maximum;

        PriceBounds(int minimum, int maximum) {
            this.minimum = minimum;
            this.maximum = maximum;
        }
    }

    public By getProductItemRoot() {
        return By.cssSelector("[data-hook=\"product-item-root\"]");
    }

    public By getBrowseByHeading() {
        return By.cssSelector("[data-hook=\"category-tree-section-title\"]");
    }

    public By getPriceSlider(String label) {
        return By.cssSelector("[role=\"slider\"][aria-label=\"" + label + "\"]");
    }

    public void navigateTo(String url) {
        driver.get(url);
    }

    public void waitForPageLoad() {
        Helpers.waitForDocumentReady(driver, TIMEOUT);
    }

    public void waitForProductGrid() {
        waitFor(getProductItemRoot(), TIMEOUT);
    }

    public String getPageTitle() {
        return driver.getTitle().trim();
    }

    public int getProductCount() {
        return driver.findElements(getProductItemRoot()).size();
    }

    /**
     * Reads every product tile currently rendered in the listing.
     */
    @SuppressWarnings("unchecked")
    public List<ListedProduct> getListedProducts() {
        Object raw = Helpers.script(driver,
                "var roots = Array.prototype.slice.call(document.querySelectorAll('[data-hook=\"product-item-root\"]'));"
                        + "return roots.map(function (root) {"
                        + "  var nameNode = root.querySelector('[data-hook=\"product-item-name\"]');"
                        + "  var priceNode = root.querySelector('[data-hook=\"product-item-price-to-pay\"]');"
                        + "  var priceText = priceNode ? (priceNode.getAttribute('data-wix-price') || priceNode.textContent) : '';"
                        + "  priceText = (priceText || '').trim();"
                        + "  return {"
                        + "    name: nameNode ? nameNode.textContent.trim() : '',"
                        + "    slug: root.getAttribute('data-slug') || '',"
                        + "    priceText: priceText"
                        + "  };"
                        + "});");

        List<ListedProduct> products = new ArrayList<>();

        if (raw == null) {
            return products;
        }

        for (Map<String, Object> entry : (List<Map<String, Object>>) raw) {
            String priceText = String.valueOf(entry.getOrDefault("priceText", ""));
            Double price = parsePrice(priceText);
            products.add(new ListedProduct(
                    String.valueOf(entry.getOrDefault("name", "")),
                    String.valueOf(entry.getOrDefault("slug", "")),
                    priceText,
                    price));
        }

        return products;
    }

    public String getBrowseByHeadingText() {
        return waitFor(getBrowseByHeading(), TIMEOUT).getText().trim();
    }

    /**
     * Reads the "Browse by" sidebar links.
     */
    @SuppressWarnings("unchecked")
    public List<SidebarCategory> getSidebarCategories() {
        Object raw = Helpers.script(driver,
                "var links = Array.prototype.slice.call(document.querySelectorAll('[data-hook^=\"category-option-\"]'));"
                        + "return links.map(function (link) {"
                        + "  return {"
                        + "    label: (link.textContent || '').trim(),"
                        + "    url: link.getAttribute('href') || ''"
                        + "  };"
                        + "});");

        List<SidebarCategory> categories = new ArrayList<>();

        if (raw == null) {
            return categories;
        }

        for (Map<String, Object> entry : (List<Map<String, Object>>) raw) {
            categories.add(new SidebarCategory(
                    String.valueOf(entry.getOrDefault("label", "")),
                    String.valueOf(entry.getOrDefault("url", ""))));
        }

        return categories;
    }

    /**
     * Reads the min/max bounds the price range slider was rendered with.
     */
    public PriceBounds getPriceFilterBounds() {
        WebElement minSlider = Helpers.wait(driver, TIMEOUT)
                .until(ExpectedConditions.presenceOfElementLocated(getPriceSlider("Minimum price")));

        int minimum = Integer.parseInt(minSlider.getAttribute("aria-valuemin"));
        int maximum = Integer.parseInt(minSlider.getAttribute("aria-valuemax"));

        return new PriceBounds(minimum, maximum);
    }

    private static Double parsePrice(String priceText) {
        if (priceText == null || priceText.isEmpty()) {
            return null;
        }

        String digits = priceText.replaceAll("[^0-9.]", "");
        return digits.isEmpty() ? null : Double.valueOf(digits);
    }
}
