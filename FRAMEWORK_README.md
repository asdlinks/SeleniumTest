# Shopbricks Selenium BDD Framework

A complete Behavior Driven Development (BDD) test automation framework for the Shopbricks e-commerce platform using Selenium WebDriver and Cucumber.js.

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

- **Node.js** v24.14.0 or higher
- **Chrome Browser** v145.x (must match ChromeDriver version)
- **npm** installed globally

### Installation

1. **Install dependencies**:
```bash
npm install
```

This installs:
- `selenium-webdriver` (v3.5.0)
- `cucumber` (BDD framework)
- `chromedriver` (v145.x - aligned with Chrome)
- `chai` (assertions)

2. **Verify ChromeDriver**:
```bash
npx chromedriver --version
```
Should output: `ChromeDriver 145.x.xxxx.xxx`

If your Chrome version differs, update `package.json`:
```json
"chromedriver": "^YOUR_CHROME_VERSION"
```
Then run `npm install` again.

## 📁 Project Structure

```
selenium-cucumber-js/
│
├── features/
│   └── shopbricks-checkout.feature          # Gherkin test scenarios
│
├── page-objects/
│   ├── home-page.js                         # Homepage navigation
│   ├── deals-offers-page.js                 # Deals section handling
│   ├── category-page.js                     # Category selection
│   ├── product-page.js                      # Product page + Add to Cart
│   ├── cart-page.js                         # Cart verification
│   ├── checkout-page.js                     # Customer form filling
│   └── order-confirmation-page.js           # Order confirmation
│
├── step-definitions/
│   └── shopbricks-steps.js                  # Gherkin step implementations
│
├── cucumber.js                              # Cucumber configuration
├── package.json                             # Dependencies
├── test.js                                  # Legacy standalone test script
└── README.md                                # This file
```

## ▶️ Running Tests

### Run All Tests
```bash
npx cucumber-js features/shopbricks-checkout.feature
```

### Run Specific Scenario
```bash
npx cucumber-js features/shopbricks-checkout.feature --name "Complete checkout flow"
```

### Run with Detailed Output
```bash
npx cucumber-js features/shopbricks-checkout.feature --format progress-bar --format json:reports/cucumber-report.json
```

### Run with Custom Timeout
By default, tests timeout after 30 seconds. To increase:
```bash
npx cucumber-js features/shopbricks-checkout.feature --timeout 60000
```

## 📊 Test Reports

After running tests, reports are generated in the `reports/` directory:

- **HTML Report**: `reports/cucumber-report.html` (open in browser)
- **JSON Report**: `reports/cucumber-report.json` (programmatic access)
- **JUnit XML**: `junit/cucumber-report.xml` (CI/CD integration)

## 🔧 Configuration

### Cucumber Config (cucumber.js)

```javascript
{
  "default": {
    "require": "step-definitions/**/*.js",
    "format": [
      "progress-bar",
      "html:reports/cucumber-report.html",
      "json:reports/cucumber-report.json"
    ],
    "formatOptions": {
      "snippetInterface": "async-await"
    }
  }
}
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

## 🎯 Test Scenario

The framework tests the "Complete checkout flow for pipe wrench purchase" scenario:

**Given**: User navigates to Shopbricks homepage
**When**: User clicks Deals & Offers → selects Hardware → finds Pipe Wrench → adds to cart
**Then**: Product appears in cart
**When**: User proceeds to checkout → fills customer details → places order
**Then**: Order confirmation page displays with Thank You message

## ⏱️ Timing & Delays

The framework includes strategic delays to handle dynamic page loading:

- **2-second delay** after clicking product category (category-page.js)
- **2-second delay** after clicking product (product-page.js)
- **2-second delay** before clicking "Add to Cart" (product-page.js)
- **1-second delay** before clicking "Proceed to Checkout" (cart-page.js)
- **1-second delay** before clicking "Place Order" (checkout-page.js)

These delays prevent "Element not clickable" errors and ensure page elements are interactive.

## 🔍 Page Objects

Each page has its own Page Object class for maintainability:

### HomePage
```javascript
navigateToHome(url)           // Navigate to homepage
clickDealsAndOffers()          // Click Deals & Offers link
waitForPageLoad()              // Wait for page ready
```

### DealsOffersPage
```javascript
selectCategory(categoryName)   // Select category (e.g., "Hardware")
isDealsOffersPageLoaded()     // Verify page loaded
```

### CategoryPage
```javascript
selectProduct(productName)     // Select product by name
waitForCategoryPageLoad()      // Wait for category page
```

### ProductPage
```javascript
addToCart()                    // Click Add to Cart with 2s delay
getProductName()               // Get product name
getProductPrice()              // Get product price
isAddToCartVisible()           // Verify button visibility
```

### CartPage
```javascript
proceedToCheckout()            // Click Checkout button
isProductInCart(productName)   // Verify item in cart
getItemCount()                 // Get cart item count
isCheckoutButtonVisible()      // Verify checkout button
```

### CheckoutPage
```javascript
fillCustomerDetails(details)   // Fill form fields (email, name, address, etc.)
placeOrder()                   // Click Place Order button
clickContinue()                // Click Continue (if present)
```

### OrderConfirmationPage
```javascript
waitForConfirmationPageLoad()  // Wait for confirmation page
isOrderConfirmed()             // Verify Thank You message
getThankYouText()              // Extract thank you message
getOrderDetails()              // Extract order details
getOrderNumber()               // Extract order number
```

## 🐛 Debugging

### View Browser in Real-Time
Currently tests run headless (background Chrome process). To watch the browser:

Edit `step-definitions/shopbricks-steps.js`, Before hook:
```javascript
const driver = await new Builder()
  .forBrowser('chrome')
  .setChromeOptions(new chrome.Options().headless(false))  // Show browser
  .build();
```

Then run tests—Chrome window will stay open.

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
| "ChromeDriver version mismatch" | Run `npm install` to sync chromedriver version |
| "Element not clickable" | Increase delay times in page objects (currently 1-2 seconds) |
| "Cannot find element" | Check Shopbricks UI structure—add fallback XPaths in page objects |
| "connection refused" | Ensure Chrome is installed and chromedriver path is correct |

## 📝 Step Definitions

Steps are defined in `step-definitions/shopbricks-steps.js`:

```javascript
Given('I navigate to the Shopbricks website', async function () { ... })
When('I click on the Deals and Offers section', async function () { ... })
And('I select the Hardware category', async function () { ... })
And('I search for and select the Pipe Wrench product', async function () { ... })
And('I add the product to the cart', async function () { ... })
Then('I should see the product added to cart notification', async function () { ... })
When('I proceed to checkout', async function () { ... })
And('I fill in the customer details:', async function (dataTable) { ... })
And('I click the Place Order button', async function () { ... })
Then('I should see the Thank You message confirming the order', async function () { ... })
And('I should see the order confirmation details', async function () { ... })
```

Before/After hooks manage WebDriver lifecycle:
- **Before**: Launches Chrome browser, initializes page objects
- **After**: Closes browser, cleans up resources

## 🔄 Retry & Error Handling

The framework includes:
- Multi-selector fallback patterns (try multiple XPaths if first fails)
- Graceful error handling (skip missing form fields instead of crashing)
- Explicit waits for page elements (15-second timeout by default)
- Promise-based async/await for clean async handling

## 🚀 Next Steps

1. **Run tests**: `npx cucumber-js`
2. **View report**: Open `reports/cucumber-report.html` in browser
3. **Add more scenarios**: Create additional `.feature` files in `features/`
4. **Add screenshots**: Extend `After` hook with `driver.takeScreenshot()`
5. **CI/CD integration**: Configure GitHub Actions / Jenkins to run tests automatically

## 📚 Additional Resources

- [Cucumber.js Documentation](https://github.com/cucumber/cucumber-js)
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
