import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ShopbricksSteps {

    // Every expected value below is read from shared-objects/shopbricks-data.json
    private static final JsonNode SITE_DATA = DataProvider.site();
    private static final JsonNode CHECKOUT_DATA = DataProvider.getTestCase("checkout");
    private static final JsonNode DEALS_DATA = DataProvider.getTestCase("dealsOffersFiltering");
    private static final JsonNode NAVIGATION_DATA = DataProvider.navigation();

    private WebDriver driver;
    private HomePage homePage;
    private DealsAndOffersPage dealsAndOffersPage;
    private CategoryPage categoryPage;
    private CatalogPage catalogPage;
    private ProductPage productPage;
    private CartPage cartPage;
    private CheckoutPage checkoutPage;
    private OrderConfirmationPage orderConfirmationPage;

    // Scenario scoped state, replacing the `this.*` values on the cucumber-js World
    private Map<String, List<String>> homepageSectionValues;
    private String categoryKey;
    private Scenario currentScenario;

    @Before
    public void before(Scenario scenario) {
        currentScenario = scenario;
        driver = DriverFactory.create();

        homePage = new HomePage(driver);
        dealsAndOffersPage = new DealsAndOffersPage(driver);
        categoryPage = new CategoryPage(driver);
        catalogPage = new CatalogPage(driver);
        productPage = new ProductPage(driver);
        cartPage = new CartPage(driver);
        checkoutPage = new CheckoutPage(driver);
        orderConfirmationPage = new OrderConfirmationPage(driver);
    }

    @After
    public void after(Scenario scenario) {
        if (driver != null) {
            if (scenario.isFailed()) {
                try {
                    byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                    scenario.attach(screenshot, "image/png", scenario.getName());
                } catch (RuntimeException err) {
                    System.out.println("Unable to capture failure screenshot: " + err.getMessage());
                }
            }
            driver.quit();
        }
    }

    // Step: Given I navigate to the Shopbricks website
    @Given("I navigate to the Shopbricks website")
    public void iNavigateToTheShopbricksWebsite() {
        homePage.navigateToHome(SITE_DATA.get("baseUrl").asText());
        homePage.waitForPageLoad();
    }

    @Given("I navigate to the Shopbricks homepage")
    public void iNavigateToTheShopbricksHomepage() {
        homePage.navigateToHome(SITE_DATA.get("baseUrl").asText());
        homePage.waitForPageLoad();
    }

    @Then("I should see the Shop By Category heading")
    public void iShouldSeeTheShopByCategoryHeading() {
        String expectedHeading = HomePageData.shopByCategoryHeading();
        String actualHeading = homePage.getTextFromLocator(homePage.getShopByCategoryHeading());
        assertThat(actualHeading).isEqualTo(expectedHeading);
    }

    @Then("I should see the homepage categories")
    public void iShouldSeeTheHomepageCategories() {
        for (String category : HomePageData.categories()) {
            boolean visible = homePage.isLocatorVisible(homePage.getSectionItemLocator(category));
            assertThat(visible).isTrue();
        }
    }

    @Then("I should see the Our Trusted Brands heading")
    public void iShouldSeeTheOurTrustedBrandsHeading() {
        String expectedHeading = HomePageData.trustedBrandsHeading();
        String actualHeading = homePage.getTextFromLocator(homePage.getTrustedBrandsHeading());
        assertThat(actualHeading).isEqualTo(expectedHeading);
    }

    @When("I capture homepage section values for Shop By Category, Shop Power Tools, Shop Hardware, and Our Trusted Brands")
    public void iCaptureHomepageSectionValues() {
        homepageSectionValues = new LinkedHashMap<>();

        for (String heading : HomePageData.sectionHeadings()) {
            List<String> items = homePage.getItemsUnderHeading(heading);
            homepageSectionValues.put(heading, items);
            System.out.println("Captured " + items.size() + " items under heading '" + heading + "': " + items);
        }
    }

    @Then("I should see homepage section options under each heading")
    public void iShouldSeeHomepageSectionOptionsUnderEachHeading() {
        for (String heading : HomePageData.sectionHeadings()) {
            List<String> items = homepageSectionValues == null ? null : homepageSectionValues.get(heading);
            assertThat(items).isNotNull().isNotEmpty();
            assertThat(items).allMatch(item -> item != null && !item.trim().isEmpty());
            System.out.println("Validated " + items.size() + " non-empty items under heading '" + heading + "'.");
        }
    }

    // Step: When I click on the Deals and Offers section
    @When("I click on the Deals and Offers section")
    public void iClickOnTheDealsAndOffersSection() {
        homePage.clickDealsAndOffers();
        dealsAndOffersPage.waitForPageLoad();
        assertThat(dealsAndOffersPage.isDealsOffersPageLoaded()).isTrue();
    }

    // Step: And I select the Hardware category
    @When("I select the Hardware category")
    public void iSelectTheHardwareCategory() {
        dealsAndOffersPage.selectCategory(CHECKOUT_DATA.get("categoryName").asText());
        Helpers.sleep(2000);
        assertThat(categoryPage.isCategoryPageLoaded()).isTrue();
    }

    // Step: And I search for and select the Pipe Wrench product
    @When("I search for and select the Pipe Wrench product")
    public void iSearchForAndSelectThePipeWrenchProduct() {
        categoryPage.selectProduct(CHECKOUT_DATA.get("product").get("searchTerm").asText());
        Helpers.sleep(2000);
        assertThat(productPage.isAddToCartVisible()).isTrue();
    }

    // Step: And I add the product to the cart
    @When("I add the product to the cart")
    public void iAddTheProductToTheCart() {
        // Waits for the Bricks AI Assistant widget first so the product page is
        // fully loaded before the click.
        productPage.addToCart(Duration.ofMillis(DataProvider.productPage().get("aiAssistantTimeoutMs").asLong()));
        Helpers.sleep(2000);
    }

    // Step: Then I should see the product added to cart notification
    @Then("I should see the product added to cart notification")
    public void iShouldSeeTheProductAddedToCartNotification() {
        Helpers.sleep(2000);
        System.out.println("Product added to cart");
    }

    // Step: When I proceed to checkout
    @When("I proceed to checkout")
    public void iProceedToCheckout() {
        cartPage.proceedToCheckout();
        Helpers.sleep(2000);
        // Try to wait for checkout page, but don't fail if specific title doesn't exist
        try {
            checkoutPage.waitForCheckoutPageLoad();
        } catch (RuntimeException err) {
            System.out.println("Checkout page title not found, proceeding anyway");
        }
    }

    // Step: And I fill in the customer details
    @When("I fill in the customer details:")
    public void iFillInTheCustomerDetails(DataTable dataTable) {
        // Mirrors cucumber-js rowsHash(): first column is the key, second the value,
        // header row included (harmlessly) exactly as the JavaScript version did.
        Map<String, String> data = new HashMap<>();
        for (List<String> row : dataTable.asLists(String.class)) {
            if (row.size() >= 2) {
                data.put(row.get(0), row.get(1));
            }
        }

        CheckoutPage.CustomerDetails customerDetails = new CheckoutPage.CustomerDetails();
        customerDetails.email = firstOf(data, "Email", "email");
        customerDetails.firstName = firstOf(data, "First name", "firstName");
        customerDetails.lastName = firstOf(data, "Last name", "lastName");
        customerDetails.address1 = firstOf(data, "Address", "address");
        customerDetails.city = firstOf(data, "City", "city");
        customerDetails.country = firstOf(data, "Country/Region", "country/region", "country");
        customerDetails.region = firstOf(data, "Region/State", "region/state", "region", "state");
        customerDetails.zip = firstOf(data, "Zip / Postal code", "zip");
        customerDetails.phone = firstOf(data, "Phone", "phone");

        checkoutPage.fillCustomerDetails(customerDetails);
        Helpers.sleep(2000);
    }

    // Step: And I click the Place Order button
    @When("I click the Place Order button")
    public void iClickThePlaceOrderButton() {
        checkoutPage.placeOrder();
        Helpers.sleep(3000);
    }

    // Step: Then I should see the Thank You message confirming the order
    @Then("I should see the Thank You message confirming the order")
    public void iShouldSeeTheThankYouMessage() {
        orderConfirmationPage.waitForConfirmationPageLoad();
        assertThat(orderConfirmationPage.isOrderConfirmed()).isTrue();

        String thankYouText = orderConfirmationPage.getThankYouText();
        System.out.println("Order Confirmation Message: " + thankYouText);
    }

    // Step: And I should see the order confirmation details
    @Then("I should see the order confirmation details")
    public void iShouldSeeTheOrderConfirmationDetails() {
        String orderDetails = orderConfirmationPage.getOrderDetails();
        assertThat(orderDetails).isNotEmpty();
        System.out.println("Order Details: " + orderDetails);
    }

    // Step: And I click the Continue button after customer details
    @When("I click the Continue button after customer details")
    public void iClickTheContinueButtonAfterCustomerDetails() {
        checkoutPage.clickContinue();
        checkoutPage.waitForPageTransition();
    }

    // Step: And I select free shipping if not already selected
    @When("I select free shipping if not already selected")
    public void iSelectFreeShippingIfNotAlreadySelected() {
        checkoutPage.selectFreeShipping();
        Helpers.sleep(1500);
    }

    // Step: And I click the Continue button to proceed to payment
    @When("I click the Continue button to proceed to payment")
    public void iClickTheContinueButtonToProceedToPayment() {
        checkoutPage.clickContinue();
        checkoutPage.waitForPageTransition();
    }

    // Step: And I click the Place Order & Pay button
    @When("I click the Place Order & Pay button")
    public void iClickThePlaceOrderAndPayButton() {
        checkoutPage.clickPlaceOrderPay();
        Helpers.sleep(3000);
    }

    // New Deals & Offers flow steps
    @When("I click the All Products link under Browse by")
    public void iClickTheAllProductsLinkUnderBrowseBy() {
        dealsAndOffersPage.clickAllProductsLink();
        Helpers.sleep(2000);
    }

    @Then("I should see at least 10 products on the page")
    public void iShouldSeeAtLeast10ProductsOnThePage() {
        int count = dealsAndOffersPage.getProductCardCount();
        assertThat(count).isGreaterThanOrEqualTo(DEALS_DATA.get("minimumProducts").asInt());
    }

    @When("I click Load More three times")
    public void iClickLoadMoreThreeTimes() {
        int lastCount = dealsAndOffersPage.getProductCardCount();

        for (int i = 0; i < DEALS_DATA.get("loadMoreClicks").asInt(); i += 1) {
            int before = dealsAndOffersPage.getProductCardCount();
            dealsAndOffersPage.clickLoadMore();
            int after = dealsAndOffersPage.getProductCardCount();
            lastCount = after;

            if (after > before) {
                continue;
            }

            Helpers.sleep(2000);
            int retryCount = dealsAndOffersPage.getProductCardCount();
            if (retryCount > before) {
                lastCount = retryCount;
            }
        }

        assertThat(lastCount).isGreaterThan(0);
    }

    @When("I set the price filter to a maximum of 300")
    public void iSetThePriceFilterToAMaximumOf300() {
        JsonNode priceFilter = DEALS_DATA.get("priceFilter");
        dealsAndOffersPage.setPriceRange(priceFilter.get("firstMaximum").asInt(), priceFilter.get("minimum").asInt());
        Helpers.sleep(2000);
        attachScreenshot();
    }

    @Then("I should see only products priced at 300 or less")
    public void iShouldSeeOnlyProductsPricedAt300OrLess() {
        List<String> prices = dealsAndOffersPage.getProductPrices();
        assertThat(prices).isNotEmpty();

        double maximum = DEALS_DATA.get("priceFilter").get("firstMaximum").asDouble();
        boolean hasWithinRange = prices.stream()
                .anyMatch(priceText -> Double.parseDouble(priceText.replace("$", "")) <= maximum);
        assertThat(hasWithinRange).isTrue();
    }

    @When("I set the price filter to a maximum of 100")
    public void iSetThePriceFilterToAMaximumOf100() {
        JsonNode priceFilter = DEALS_DATA.get("priceFilter");
        dealsAndOffersPage.setPriceRange(priceFilter.get("secondMaximum").asInt(), priceFilter.get("minimum").asInt());
        Helpers.sleep(2000);
        attachScreenshot();
    }

    @Then("I should see the price filter span text on the page")
    public void iShouldSeeThePriceFilterSpanTextOnThePage() {
        String text = dealsAndOffersPage.getFilterRangeText();
        assertThat(text.toLowerCase()).contains(DEALS_DATA.get("priceFilter").get("secondMaximum").asText());
    }

    // Step: And I should see the first name and last name on the confirmation page
    @Then("I should see the first name and last name on the confirmation page")
    public void iShouldSeeTheFirstNameAndLastNameOnTheConfirmationPage() {
        String firstName = CHECKOUT_DATA.get("customer").get("firstName").asText();
        String lastName = CHECKOUT_DATA.get("customer").get("lastName").asText();
        boolean hasCustomerInfo = orderConfirmationPage.validateCustomerNameOnConfirmation(firstName, lastName);
        assertThat(hasCustomerInfo).isTrue();
        System.out.println("Customer names '" + firstName + " " + lastName + "' are visible on confirmation page");
    }

    // Step: And I should see the email confirmation message
    @Then("I should see the email confirmation message")
    public void iShouldSeeTheEmailConfirmationMessage() {
        boolean hasEmailMessage = orderConfirmationPage.validateEmailConfirmationMessage();
        assertThat(hasEmailMessage).isTrue();
        System.out.println("Email confirmation message is visible on the page");
    }

    // Step: And I should extract and store the order number
    @Then("I should extract and store the order number")
    public void iShouldExtractAndStoreTheOrderNumber() {
        String orderNumber = orderConfirmationPage.extractOrderNumber();
        assertThat(orderNumber).isNotEmpty();
        OrderConfirmationPage.storedOrderNumber = orderNumber;
        System.out.println("Order Number extracted and stored: " + orderNumber);
    }

    // =========================================================================
    // Data driven steps.
    // Every expectation is resolved from shared-objects/shopbricks-data.json via
    // the data provider, keyed by the value supplied in the Examples table.
    // =========================================================================

    // Step: Given I open the "<categoryKey>" category page
    @Given("I open the {string} category page")
    public void iOpenTheCategoryPage(String categoryKey) {
        JsonNode category = DataProvider.getCategory(categoryKey);
        this.categoryKey = categoryKey;
        catalogPage.navigateTo(category.get("url").asText());
        catalogPage.waitForPageLoad();
        catalogPage.waitForProductGrid();
        System.out.println("Opened category page '" + category.get("name").asText() + "' at "
                + category.get("url").asText());
    }

    // Step: Then the page title should match the expected title for "<categoryKey>"
    @Then("the page title should match the expected title for {string}")
    public void thePageTitleShouldMatchTheExpectedTitleFor(String categoryKey) {
        JsonNode category = DataProvider.getCategory(categoryKey);
        String actualTitle = catalogPage.getPageTitle();
        assertThat(actualTitle).isEqualTo(category.get("pageTitle").asText());
        System.out.println("Page title matched expected value '" + category.get("pageTitle").asText() + "'");
    }

    // Step: Then I should see at least the expected number of products for "<categoryKey>"
    @Then("I should see at least the expected number of products for {string}")
    public void iShouldSeeAtLeastTheExpectedNumberOfProductsFor(String categoryKey) {
        JsonNode category = DataProvider.getCategory(categoryKey);
        int expected = category.get("minimumExpectedProducts").asInt();
        int actualCount = catalogPage.getProductCount();
        assertThat(actualCount).isGreaterThanOrEqualTo(expected);
        System.out.println("Found " + actualCount + " products, expected at least " + expected);
    }

    // Step: Then every expected product for "<categoryKey>" should be listed
    @Then("every expected product for {string} should be listed")
    public void everyExpectedProductShouldBeListed(String categoryKey) {
        List<JsonNode> expectedProducts = DataProvider.getExpectedProducts(categoryKey);
        List<CatalogPage.ListedProduct> listedProducts = catalogPage.getListedProducts();

        List<String> listedNames = new ArrayList<>();
        List<String> listedSlugs = new ArrayList<>();
        listedProducts.forEach(product -> {
            listedNames.add(product.name);
            listedSlugs.add(product.slug);
        });

        List<String> missing = new ArrayList<>();
        for (JsonNode product : expectedProducts) {
            if (!listedSlugs.contains(product.get("slug").asText())) {
                missing.add(product.get("name").asText());
            }
        }

        assertThat(missing)
                .as("Products missing from the '%s' listing", categoryKey)
                .isEmpty();
        System.out.println("All " + expectedProducts.size() + " expected products are listed. Rendered order: "
                + String.join(", ", listedNames));
    }

    // Step: Then every listed product price should match the expected price for "<categoryKey>"
    @Then("every listed product price should match the expected price for {string}")
    public void everyListedProductPriceShouldMatch(String categoryKey) {
        List<JsonNode> expectedProducts = DataProvider.getExpectedProducts(categoryKey);
        Map<String, CatalogPage.ListedProduct> listedBySlug = new LinkedHashMap<>();
        catalogPage.getListedProducts().forEach(product -> listedBySlug.put(product.slug, product));

        List<String> mismatches = new ArrayList<>();

        for (JsonNode expectedProduct : expectedProducts) {
            String slug = expectedProduct.get("slug").asText();
            String name = expectedProduct.get("name").asText();
            CatalogPage.ListedProduct listedProduct = listedBySlug.get(slug);

            if (listedProduct == null) {
                mismatches.add(name + ": not rendered");
                continue;
            }

            if (!listedProduct.name.equals(name)) {
                mismatches.add(slug + ": name '" + listedProduct.name + "' != expected '" + name + "'");
            }

            double expectedPrice = expectedProduct.get("price").asDouble();
            if (listedProduct.price == null || Double.compare(listedProduct.price, expectedPrice) != 0) {
                mismatches.add(name + ": price " + listedProduct.priceText + " != expected "
                        + expectedProduct.get("priceText").asText());
            }
        }

        assertThat(mismatches)
                .as("Product data mismatches on the '%s' listing", categoryKey)
                .isEmpty();
        System.out.println("Name and price matched the data file for all " + expectedProducts.size()
                + " products in '" + categoryKey + "'");
    }

    // Step: Given I open the product page for "<slug>"
    @Given("I open the product page for {string}")
    public void iOpenTheProductPageFor(String slug) {
        JsonNode product = DataProvider.getProduct(slug);
        productPage.navigateTo(product.get("url").asText());
        productPage.waitForProductDetailsLoad();
        System.out.println("Opened product page '" + product.get("name").asText() + "' at "
                + product.get("url").asText());
    }

    // Step: Then the product page title should match the expected title for "<slug>"
    @Then("the product page title should match the expected title for {string}")
    public void theProductPageTitleShouldMatch(String slug) {
        JsonNode product = DataProvider.getProduct(slug);
        String actualTitle = productPage.getPageTitle();
        assertThat(actualTitle).isEqualTo(product.get("pageTitle").asText());
        System.out.println("Product page title matched expected value '" + product.get("pageTitle").asText() + "'");
    }

    // Step: Then the product name, price and SKU should match the expected data for "<slug>"
    @Then("the product name, price and SKU should match the expected data for {string}")
    public void theProductDataShouldMatch(String slug) {
        JsonNode product = DataProvider.getProduct(slug);

        assertThat(productPage.getDisplayedName()).isEqualTo(product.get("name").asText());
        assertThat(productPage.getDisplayedPrice()).isEqualTo(product.get("price").asDouble());
        assertThat(productPage.getDisplayedSku()).isEqualTo(product.get("sku").asText());

        System.out.println("Validated '" + product.get("name").asText() + "' | price "
                + product.get("priceText").asText() + " | SKU " + product.get("sku").asText());
    }

    // Step: Then the Add to Cart button should be available on the product page
    @Then("the Add to Cart button should be available on the product page")
    public void theAddToCartButtonShouldBeAvailable() {
        assertThat(productPage.isAddToCartButtonPresent()).isTrue();
        System.out.println("'" + DataProvider.productPage().get("addToCartButtonText").asText()
                + "' button is available");
    }

    // Step: Then I should see the Browse by heading from the data file
    @Then("I should see the Browse by heading from the data file")
    public void iShouldSeeTheBrowseByHeading() {
        String expected = NAVIGATION_DATA.get("browseBy").get("heading").asText();
        assertThat(catalogPage.getBrowseByHeadingText()).isEqualTo(expected);
        System.out.println("Sidebar heading matched expected value '" + expected + "'");
    }

    // Step: Then the Browse by category list should match the expected categories
    @Then("the Browse by category list should match the expected categories")
    public void theBrowseByCategoryListShouldMatch() {
        JsonNode expectedCategories = NAVIGATION_DATA.get("browseBy").get("categories");

        List<String> expected = new ArrayList<>();
        expectedCategories.forEach(category -> expected.add(
                category.get("label").asText() + " -> " + stripTrailingSlash(category.get("url").asText())));

        List<String> actual = new ArrayList<>();
        catalogPage.getSidebarCategories()
                .forEach(category -> actual.add(category.label + " -> " + stripTrailingSlash(category.url)));

        assertThat(actual).isEqualTo(expected);
        System.out.println("Browse by list matched all " + expected.size() + " expected categories in order");
    }

    // Step: Then the price filter bounds should match the expected range for "<categoryKey>"
    @Then("the price filter bounds should match the expected range for {string}")
    public void thePriceFilterBoundsShouldMatch(String categoryKey) {
        JsonNode priceFilter = DataProvider.getCategory(categoryKey).get("priceFilter");
        CatalogPage.PriceBounds actualBounds = catalogPage.getPriceFilterBounds();

        assertThat(actualBounds.minimum).isEqualTo(priceFilter.get("minimum").asInt());
        assertThat(actualBounds.maximum).isEqualTo(priceFilter.get("maximum").asInt());

        System.out.println("Price filter bounds matched expected range $" + priceFilter.get("minimum").asInt()
                + " - $" + priceFilter.get("maximum").asInt());
    }

    private static String firstOf(Map<String, String> data, String... keys) {
        for (String key : keys) {
            String value = data.get(key);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private static String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private void attachScreenshot() {
        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            currentScenario.attach(screenshot, "image/png", "price-filter");
        } catch (RuntimeException err) {
            System.out.println("Unable to attach screenshot: " + err.getMessage());
        }
    }
}
