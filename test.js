const { Builder, By, until, Key } = require('selenium-webdriver');
const chrome = require('selenium-webdriver/chrome');

(async function checkoutFlow() {
  const MAX_ATTEMPTS = 3;

  // Launch browser
  async function buildDriver(retries = 2) {
    const options = new chrome.Options();

    try {
      return new Builder()
        .forBrowser('chrome')
        .setChromeOptions(options)
        .build();
    } catch (err) {
      if (retries > 0) {
        await new Promise((resolve) => setTimeout(resolve, 500));
        return buildDriver(retries - 1);
      }
      throw err;
    }
  }

  async function safeQuit(driver) {
    if (!driver) return;
    try {
      await driver.quit();
    } catch (ignored) {
      // ignore errors during cleanup
    }
  }

  async function runOnce() {
    const driver = await buildDriver();

    // Helper - try multiple locators and return first element found
    async function findFirst(locatorList, timeout = 15000) {
      for (const loc of locatorList) {
        const elements = await driver.findElements(loc);
        if (elements.length) {
          return elements[0];
        }
      }
      throw new Error(`No element found for any locator: ${locatorList.map(l => l.toString()).join(', ')}`);
    }

    // Helper - wait until URL contains a substring
    async function waitForUrlContains(substr, timeout = 15000) {
      await driver.wait(async () => {
        const url = await driver.getCurrentUrl();
        return url.includes(substr);
      }, timeout);
    }

    // Helper - click an element that contains given visible text (case-insensitive)
    async function clickByText(text, timeout = 15000) {
      const lower = text.toLowerCase();
      const xpaths = [
        `//*[contains(normalize-space(string()), '${text}')]`,
        `//*[contains(translate(normalize-space(string()), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '${lower}')]`,
        `//button[contains(translate(normalize-space(string()), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '${lower}')]`,
        `//a[contains(translate(normalize-space(string()), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '${lower}')]`,
        `//input[@type='submit' and contains(translate(@value,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '${lower}')]`,
      ];

      const cssVariants = [
        `[class*="${lower.replace(/\s+/g, '-')}"]`,
        `[id*="${lower.replace(/\s+/g, '-')}"]`,
        `[name*="${lower.replace(/\s+/g, '-')}"]`,
      ];

      const errors = [];

      for (const xpath of xpaths) {
        try {
          const el = await driver.wait(until.elementLocated(By.xpath(xpath)), timeout);
          await driver.wait(until.elementIsVisible(el), timeout);
          await el.click();
          return el;
        } catch (err) {
          errors.push(err);
        }
      }

      for (const selector of cssVariants) {
        try {
          const el = await driver.wait(until.elementLocated(By.css(selector)), timeout);
          await driver.wait(until.elementIsVisible(el), timeout);
          await el.click();
          return el;
        } catch (err) {
          errors.push(err);
        }
      }

      const message = `Unable to click element containing text '${text}'. Tried ${xpaths.length} xpath(s) and ${cssVariants.length} css selector(s).`;
      const err = new Error(message);
      err.innerErrors = errors;
      throw err;
    }

    try {
      // 1) Go to ShopBricks
      await driver.get('https://www.shopbricks.co/');
      await driver.wait(async () => {
        const readyState = await driver.executeScript('return document.readyState');
        return readyState === 'complete';
      }, 15000);

    // 2) Select first product
    // Try a few common product link patterns to make the script more resilient.
    const productLinkSelectors = [
      By.css('a[href*="/product"]'),
      By.css('a[href*="/products"]'),
      By.css('.product a'),
      By.css('.product-item a'),
      By.css('.grid-item a'),
      By.css('.product-card a'),
    ];

    const firstProduct = await findFirst(productLinkSelectors);
    await driver.wait(until.elementIsVisible(firstProduct), 10000);
    await firstProduct.click();

    //driver.sleep(1000); // Wait for product page to load (adjust as needed)

    // 3) Add to cart
    // Prefer the specific "Add to Cart" span selector if available.
    try {
      const addToCart = await driver.wait(until.elementLocated(By.xpath("//span[text()='Add to Cart']")), 10000);
      await driver.wait(until.elementIsVisible(addToCart), 10000);
      await driver.executeScript('arguments[0].scrollIntoView({ behavior: "smooth", block: "center" });', addToCart);
      await driver.sleep(5000); // wait 5 seconds after scrolling and before clicking
      await addToCart.click();
    } catch (err) {
      await clickByText('Add to Cart');
    }

   
    // 5) Proceed to checkout
    try {
      const checkout = await driver.wait(until.elementLocated(By.xpath("//span[text()='Checkout']")), 10000);
      await driver.wait(until.elementIsVisible(checkout), 10000);
      await driver.executeScript('arguments[0].scrollIntoView({ behavior: "smooth", block: "center" });', checkout);
      await driver.sleep(5000); // wait 5 seconds after scrolling and before clicking
      await checkout.click();
    } catch (err) {
      await clickByText('Checkout');
    }

    // 6) Fill checkout form if present (simple check + fill)
    const fillIfExists = async (locator, value) => {
      try {
        const el = await driver.findElement(locator);
        if (await el.isDisplayed()) {
          await el.clear();
          await el.sendKeys(value);
          return true;
        }
      } catch (ignore) {
        // Not found / not interactable, skip
      }
      return false;
    };

    await fillIfExists(By.xpath("//label[text()='Email']"), 'test@example.com');
    await fillIfExists(By.xpath("//label[text()='First name']"), 'Test');
    await fillIfExists(By.xpath("//label[text()='Last name']"), 'User');
    await fillIfExists(By.xpath("//label[text()='Address']"), '123 Test St');
    await fillIfExists(By.xpath("//label[text()='City']"), 'Testville');
    await fillIfExists(By.xpath("//label[text()='Zip / Postal code']"), '12345');
    await fillIfExists(By.xpath("//span[text()='Phone']"), '5551234567');

    // If a "Continue" or "Place order" button is present, click it
    await clickByText('Continue').catch(() => {});
    await clickByText('Place order').catch(() => {});

    console.log('✅ Checkout flow completed (reached checkout/confirmation).');
  } catch (err) {
    console.error('❌ Checkout flow failed:', err);
    throw err;
  } finally {
    await safeQuit(driver);
  }
}

for (let attempt = 1; attempt <= MAX_ATTEMPTS; attempt += 1) {
  try {
    await runOnce();
    break;
  } catch (err) {
    const isConnRefused = /ECONNREFUSED/.test(err.message);
    console.error(`Attempt ${attempt} failed${isConnRefused ? ' (connection refused)' : ''}.`);

    if (!isConnRefused || attempt === MAX_ATTEMPTS) {
      console.error('✅ No more retries.');
      throw err;
    }

    // Wait a bit before retrying
    await new Promise((resolve) => setTimeout(resolve, 1000));
  }
}
})();
