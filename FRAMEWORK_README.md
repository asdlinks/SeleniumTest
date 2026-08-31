# Shopbricks Selenium BDD Framework

A complete Behavior Driven Development (BDD) test automation framework for the Shopbricks e-commerce platform using Selenium WebDriver and Cucumber JVM.

## 📋 Framework Overview

This framework tests the complete checkout flow for https://www.shopbricks.co/:

```
Home Page 
  → Deals & Offers Section 
    → Hardware Category 
      → Product Selection (Pipe Wrench) 
        → Add to Cart 
          → Cart Page 
            → Proceed to Checkout 
              → Fill Customer Details 
                → Place Order 
                  → Order Confirmation
```

## 🚀 Quick Start

### Prerequisites

- **JDK 17 or higher** (built and verified on JDK 25)
- **Chrome Browser** (any recent version)
- Maven is **not** required — the repo ships the Maven Wrapper

### Installation

There is no install step. The first run downloads Maven and the dependencies:

```bash
./mvnw test          # macOS / Linux
.\mvnw.cmd test      # Windows
```

This resolves:
- `selenium-java` 4.27.0
- `cucumber-java` + `cucumber-junit-platform-engine` 7.20.1
- `jackson-databind` (reads the JSON test data)
- `assertj-core` (assertions)

### No driver management needed

Selenium 4 ships **Selenium Manager**, which downloads and matches the browser
driver to the installed Chrome automatically. There is no ChromeDriver version
to keep in step with your browser — the version mismatch that plagued the
JavaScript framework is gone.

## 📁 Project Structure

The JavaScript layout is preserved exactly — each layer keeps its own top level
folder. Java has no hyphens in package names, so every class lives in the
**default package** and each folder is registered as a source root in `pom.xml`
via `build-helper-maven-plugin`.

```
selenium-cucumber-java/
│
├── features/
│   ├── shopbricks-checkout.feature          # End to end checkout scenario
│   ├── deals-offers-filtering.feature       # Filtering / load more scenario
│   └── catalog-data-validation.feature      # Data driven scenarios
│
├── page-objects/
│   ├── HomePage.java                        # Homepage navigation
│   ├── DealsAndOffersPage.java              # Deals section handling
│   ├── CategoryPage.java                    # Category selection
│   ├── CatalogPage.java                     # Generic listing page reader
│   ├── ProductPage.java                     # Product page + Add to Cart
│   ├── CartPage.java                        # Cart verification
│   ├── CheckoutPage.java                    # Customer form filling
│   └── OrderConfirmationPage.java           # Order confirmation
│
├── shared-objects/
│   ├── shopbricks-data.json                 # 👈 Single source of test data
│   ├── DataProvider.java                    # Lookup helpers over the JSON
│   ├── HomePageData.java                    # Homepage expectations (from JSON)
│   └── TestData.java                        # Credentials (from JSON)
│
├── step-definitions/
│   └── ShopbricksSteps.java                 # Gherkin step implementations + hooks
│
├── runtime/
│   ├── DriverFactory.java                   # Builds the WebDriver
│   ├── Helpers.java                         # Shared browser utilities
│   └── RunCucumberTest.java                 # Suite entry point + report config
│
├── pom.xml                                  # Dependencies and source roots
├── mvnw / mvnw.cmd                          # Maven Wrapper
└── FRAMEWORK_README.md                      # This file
```

## ▶️ Running Tests

On Windows use `.\mvnw.cmd` in place of `./mvnw` below.

### Run All Tests
```bash
./mvnw test
```

### Run by Tag
```bash
./mvnw test -Dcucumber.filter.tags="@data-driven"
./mvnw test -Dcucumber.filter.tags="@catalog-data or @product-data"
./mvnw test -Dcucumber.filter.tags="not @deals-offers-flow"
```

### Run a Single Feature
```bash
./mvnw test -Dcucumber.features=features/catalog-data-validation.feature
```

### Run a Single Scenario by Name
```bash
./mvnw test -Dcucumber.filter.name="Complete checkout flow"
```

### Choose a Browser / Run Headless
```bash
./mvnw test -Dbrowser=firefox
./mvnw test -Dheadless=true
```

## 📊 Test Reports

After running tests, reports are generated in the `reports/` directory:

- **HTML Report**: `reports/cucumber-report.html` (open in browser)
- **JSON Report**: `reports/cucumber-report.json` (programmatic access)
- **JUnit XML**: `reports/junit-report.xml` (CI/CD integration)
- **Surefire**: `target/surefire-reports/` (Maven test output)

## 🔧 Configuration

### Cucumber Config (runtime/RunCucumberTest.java)

```java
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty,"
                + "json:reports/cucumber-report.json,"
                + "html:reports/cucumber-report.html,"
                + "junit:reports/junit-report.xml")
public class RunCucumberTest {
}
```

The empty glue value means "the default package", which is where every class in
this framework lives. Source roots are wired up in `pom.xml`:

```xml
<sources>
    <source>${project.basedir}/page-objects</source>
    <source>${project.basedir}/shared-objects</source>
    <source>${project.basedir}/step-definitions</source>
    <source>${project.basedir}/runtime</source>
</sources>
```

### Test Data Customization

Edit the test data in `features/shopbricks-checkout.feature`:

```gherkin
And I fill in the customer details:
  | Field     | Value            |
  | email     | test@example.com |
  | firstName | John             |
  | lastName  | Doe              |
  | address1  | 123 Main St      |
  | city      | Springfield      |
  | zip       | 12345            |
  | phone     | 555-1234567      |
```

Replace email, name, address with your test data.

## 🗄️ Central Test Data (`shared-objects/shopbricks-data.json`)

Every expected value used by the framework lives in **one** JSON file. Nothing in
the step definitions or page objects hard-codes a URL, title, product name, price
or filter limit any more — they all resolve through
`shared-objects/DataProvider.java`.

| Block | What it holds |
| --- | --- |
| `site` | Base URL, store name, title suffix, currency |
| `pages` | Key, name, path, URL and expected `<title>` for each static page |
| `navigation` | Header links, the `Browse by` sidebar list, filter/sort labels, footer departments |
| `categories` | Per category: name, URL, page title, product count, price-slider bounds, and the full expected product list (name, slug, SKU, price) |
| `products` | Master catalogue of 69 products keyed by slug (name, SKU, price, URL, page title, parent categories) |
| `productPage` | Static product-page labels (`Add to Cart`, `SKU:`, `PRODUCT INFO`) |
| `homePageContent` | Homepage section headings, category tiles, trending products |
| `homepage` | Legacy homepage expectations consumed by `HomePageData.java` |
| `credentials` | Login credentials consumed by `TestData.java` |
| `testCases` | Per-scenario inputs: checkout customer details, filter limits, load-more clicks, and the category keys / product slugs that drive the `Scenario Outline` examples |

### Reading data in a step definition

```java
JsonNode category = DataProvider.getCategory("hardware");   // one category block
JsonNode product  = DataProvider.getProduct("pipe-wrench-8-in-length");
JsonNode checkout = DataProvider.getTestCase("checkout");   // scenario inputs

String title = category.get("pageTitle").asText();
double price = product.get("price").asDouble();
```

Helpers available: `getCategory`, `getCategoryUrl`, `getExpectedProducts`,
`getExpectedProductNames`, `getProduct`, `getPage`, `getTestCase`, plus the raw
blocks (`site()`, `pages()`, `navigation()`, `categories()`, `products()`, …).
The JSON is loaded once from the classpath and cached.

> The data file mirrors the live store. When the catalogue changes, update
> `shopbricks-data.json` — no step definition or page object needs editing.

## 🎯 Test Scenarios

### 1. Complete checkout flow for pipe wrench purchase

**Given**: User navigates to Shopbricks homepage
**When**: User clicks Deals & Offers → selects Hardware → finds Pipe Wrench → adds to cart
**Then**: Product appears in cart
**When**: User proceeds to checkout → fills customer details → places order
**Then**: Order confirmation page displays with Thank You message

### 2. Deals & Offers filtering and load more

Validates the product count, three `Load More` clicks and the price-range slider
on the Deals & Offers listing. Limits come from `testCases.dealsOffersFiltering`.

### 3. Data driven catalogue validation (`Scenario Outline`)

For each category key in the `Examples` table, opens the listing page and asserts
the page `<title>`, the product count, that every expected product is rendered,
and that every rendered price matches the JSON.

```bash
./mvnw test -Dcucumber.filter.tags="@catalog-data"
```

### 4. Data driven product detail validation (`Scenario Outline`)

For each product slug in the `Examples` table, opens the product page and asserts
the page `<title>`, product name, price, SKU and the presence of `Add to Cart`.

```bash
./mvnw test -Dcucumber.filter.tags="@product-data"
```

### 5. Data driven navigation and filter bounds validation

Asserts the `Browse by` heading, that the sidebar category list matches the
expected labels **and order**, and that the price slider was rendered with the
expected minimum/maximum bounds.

```bash
./mvnw test -Dcucumber.filter.tags="@navigation-data"
```

Run all three data driven scenarios together:

```bash
./mvnw test -Dcucumber.filter.tags="@data-driven"
```

## ⏱️ Timing & Delays

The framework includes strategic delays to handle dynamic page loading:

- **2-second delay** after clicking product category (CategoryPage.java)
- **2-second delay** after clicking product (ProductPage.java)
- **Wait for the "Bricks AI Assistant" widget** before clicking "Add to Cart" (ProductPage.java).
  The widget is the last thing the product page renders (~10s after load), so its
  appearance is a reliable signal that the page has finished hydrating. Without it
  the click lands too early and the item is never actually added to the cart.
  Timeout comes from `productPage.aiAssistantTimeoutMs` in `shopbricks-data.json`;
  if the widget never appears the wait is logged and the test continues.
- **2-second delay** before clicking "Add to Cart" (ProductPage.java)
- **1-second delay** before clicking "Proceed to Checkout" (CartPage.java)
- **1-second delay** before clicking "Place Order" (CheckoutPage.java)

These delays prevent "Element not clickable" errors and ensure page elements are interactive.

## 🔍 Page Objects

Each page has its own Page Object class for maintainability:

### HomePage
```java
navigateToHome(url)           // Navigate to homepage
clickDealsAndOffers()          // Click Deals & Offers link
waitForPageLoad()              // Wait for page ready
```

### DealsOffersPage
```java
selectCategory(categoryName)   // Select category (e.g., "Hardware")
isDealsOffersPageLoaded()     // Verify page loaded
```

### CategoryPage
```java
selectProduct(productName)     // Select product by name
waitForCategoryPageLoad()      // Wait for category page
```

### CatalogPage (data driven scenarios)
```java
navigateTo(url)                // Open any /category/<key> listing page
waitForProductGrid()           // Wait for the first product tile
getPageTitle()                 // Read document.title
getProductCount()              // Count rendered product tiles
getListedProducts()            // [{ name, slug, priceText, price }] per tile
getBrowseByHeadingText()       // Read the sidebar heading
getSidebarCategories()         // [{ label, url }] for the Browse by links
getPriceFilterBounds()         // { minimum, maximum } of the price slider
```

### ProductPage
```java
addToCart()                    // Click Add to Cart with 2s delay
getProductName()               // Get product name
getProductPrice()              // Get product price
isAddToCartVisible()           // Verify button visibility

// Data driven helpers (read via stable data-hook attributes)
navigateTo(url)                // Open a product page directly
waitForProductDetailsLoad()    // Wait for the product title
getPageTitle()                 // Read document.title
getDisplayedName()             // Product name shown on the page
getDisplayedPrice()            // Numeric price shown on the page
getDisplayedSku()              // SKU with the "SKU:" prefix stripped
isAddToCartButtonPresent()     // Verify the Add to Cart button
```

### CartPage
```java
proceedToCheckout()            // Click Checkout button
isProductInCart(productName)   // Verify item in cart
getItemCount()                 // Get cart item count
isCheckoutButtonVisible()      // Verify checkout button
```

### CheckoutPage
```java
fillCustomerDetails(details)   // Fill form fields (email, name, address, etc.)
placeOrder()                   // Click Place Order button
clickContinue()                // Click Continue (if present)
```

### OrderConfirmationPage
```java
waitForConfirmationPageLoad()  // Wait for confirmation page
isOrderConfirmed()             // Verify Thank You message
getThankYouText()              // Extract thank you message
getOrderDetails()              // Extract order details
getOrderNumber()               // Extract order number
```

## 🐛 Debugging

### Headed vs Headless
Tests run headed by default so you can watch the browser. To run headless:

```bash
./mvnw test -Dheadless=true
```

Both modes are handled by `runtime/DriverFactory.java`:
```java
ChromeOptions options = new ChromeOptions();
options.addArguments("--start-maximized", "--disable-extensions");

if (headless) {
    options.addArguments("--headless=new", "--window-size=1920,1080");
}

return new ChromeDriver(options);
```

### Enable Detailed Console Logging
Page objects already log major steps:
```
✓ Navigated to https://www.shopbricks.co/
✓ Clicked on Deals & Offers
[checkout-page] Filling customer details...
[checkout-page] ✓ Filled email: test@example.com
...
```

Watch console output to trace each step.

### Common Issues

| Issue | Solution |
|-------|----------|
| "ChromeDriver version mismatch" | Should not happen — Selenium Manager matches the driver to Chrome automatically |
| "Element not clickable" | Increase delay times in page objects (currently 1-2 seconds) |
| "Cannot find element" | Check Shopbricks UI structure—add fallback XPaths in page objects |
| "connection refused" | Ensure Chrome is installed |
| "No features found" | Run `./mvnw clean test` so `features/` is re-copied onto the classpath |
| Undefined steps | Check the class is in the **default package** (no `package` line) |

## 📝 Step Definitions

Steps are defined in `step-definitions/ShopbricksSteps.java`:

```java
@Given("I navigate to the Shopbricks website")
public void iNavigateToTheShopbricksWebsite() { ... }

@When("I click on the Deals and Offers section")
public void iClickOnTheDealsAndOffersSection() { ... }

@When("I select the Hardware category")
public void iSelectTheHardwareCategory() { ... }

@When("I fill in the customer details:")
public void iFillInTheCustomerDetails(DataTable dataTable) { ... }

@Then("I should see the Thank You message confirming the order")
public void iShouldSeeTheThankYouMessage() { ... }
```

Parameterised (data driven) steps use Cucumber Expressions:

```java
@Given("I open the {string} category page")
public void iOpenTheCategoryPage(String categoryKey) { ... }
```

`@Before` / `@After` hooks live in the same class, so Cucumber creates a fresh
instance — and therefore a fresh WebDriver — for every scenario, exactly as the
JavaScript `Before`/`After` hooks did.

Before/After hooks manage WebDriver lifecycle:
- **Before**: Launches Chrome browser, initializes page objects
- **After**: Closes browser, cleans up resources

## 🔄 Retry & Error Handling

The framework includes:
- Multi-selector fallback patterns (try multiple XPaths if first fails)
- Graceful error handling (skip missing form fields instead of crashing)
- Explicit waits for page elements (15-second timeout by default)
- Explicit `WebDriverWait` conditions instead of implicit waits

## 🚀 Next Steps

1. **Run tests**: `./mvnw test`
2. **View report**: Open `reports/cucumber-report.html` in browser
3. **Add more scenarios**: Create additional `.feature` files in `features/`
4. **Add screenshots**: Extend `After` hook with `driver.takeScreenshot()`
5. **CI/CD integration**: Configure GitHub Actions / Jenkins to run tests automatically

## 📚 Additional Resources

- [Cucumber JVM Documentation](https://github.com/cucumber/cucumber-jvm)
- [Cucumber Expressions](https://github.com/cucumber/cucumber-expressions)
- [Selenium WebDriver API](https://www.selenium.dev/documentation/webdriver/getting_started/install_libraries/)
- [Gherkin Syntax](https://cucumber.io/docs/gherkin/)
- [Page Object Model Pattern](https://www.selenium.dev/documentation/webdriver/pom/)

## 💡 Tips

- **Parameterize test data**: Use Gherkin data tables (already done in feature file)
- **Reuse page objects**: All page classes follow same pattern—easy to extend
- **Add new scenarios**: Create new `.feature` files in `features/`; steps will auto-discover
- **Handle UI changes**: Multi-selector pattern in page objects handles Shopbricks variations

---

**Last Updated**: March 27, 2025
**Framework Version**: 1.0
**Status**: ✅ Ready for production use
