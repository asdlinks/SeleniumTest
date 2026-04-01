const { By, until, Key } = require('selenium-webdriver');

class CheckoutPage {
  constructor(driver) {
    this.driver = driver;
  }

  // Locators
  getCheckoutTitle() {
    return By.xpath("//h1[contains(translate(., 'CHECKOUT', 'checkout'), 'checkout')] | //h2[contains(translate(., 'CHECKOUT', 'checkout'), 'checkout')] | //span[contains(translate(., 'CHECKOUT', 'checkout'), 'checkout')]");
  }

  getFieldLocators(fieldKey) {
    const f = fieldKey.toLowerCase();
    const map = {
      email: [
        By.name('email'),
        By.name('Email'),
        By.css('input[type="email"]'),
        By.xpath("//label[contains(translate(., 'EMAIL', 'email'), 'email')]/following::input[1]"),
        By.xpath("//input[contains(translate(@placeholder, 'EMAIL', 'email'), 'email')]")
      ],
      firstname: [
        By.name('firstName'),
        By.name('first_name'),
        By.name('firstname'),
        By.name('First name'),
        By.xpath("//label[contains(translate(., 'FIRST', 'first'), 'first') and contains(translate(., 'NAME', 'name'), 'name')]/following::input[1]"),
        By.xpath("//input[contains(translate(@placeholder, 'FIRST', 'first'), 'first')]")
      ],
      lastname: [
        By.name('lastName'),
        By.name('last_name'),
        By.name('lastname'),
        By.name('Last name'),
        By.xpath("//label[contains(translate(., 'LAST', 'last'), 'last') and contains(translate(., 'NAME', 'name'), 'name')]/following::input[1]"),
        By.xpath("//input[contains(translate(@placeholder, 'LAST', 'last'), 'last')]")
      ],
      address1: [
        By.name('address1'),
        By.name('address'),
        By.name('Address'),
        By.xpath("//label[contains(translate(., 'ADDRESS', 'address'), 'address')]/following::input[1]"),
        By.xpath("//input[contains(translate(@placeholder, 'ADDRESS', 'address'), 'address')]")
      ],
      city: [
        By.name('city'),
        By.name('City'),
        By.xpath("//label[contains(translate(., 'CITY', 'city'), 'city')]/following::input[1]"),
        By.xpath("//input[contains(translate(@placeholder, 'CITY', 'city'), 'city')]")
      ],
      zip: [
        By.name('zip'),
        By.name('postalCode'),
        By.name('Zip'),
        By.name('PostalCode'),
        By.name('postal_code'),
        By.xpath("//label[contains(translate(., 'ZIP', 'zip'), 'zip') or contains(translate(., 'POSTAL', 'postal'), 'postal')]/following::input[1]"),
        By.xpath("//input[contains(translate(@placeholder, 'ZIP', 'zip'), 'zip') or contains(translate(@placeholder, 'POSTAL', 'postal'), 'postal')]")
      ],
      phone: [
        By.name('phone'),
        By.name('Phone'),
        By.name('mobile'),
        By.xpath("//label[contains(translate(., 'PHONE', 'phone'), 'phone')]/following::input[1]"),
        By.xpath("//input[contains(translate(@placeholder, 'PHONE', 'phone'), 'phone')]")
      ],
      country: [
        By.name('country'),
        By.name('Country'),
        By.css('select[name="country"]'),
        By.xpath("//label[contains(translate(normalize-space(.), 'Country/Region', 'country/region'), 'country/region')]/following::select[1]"),
        By.xpath("//label[contains(translate(normalize-space(.), 'Country/Region', 'country/region'), 'country/region')]/following::input[@role='combobox'][1]"),
        By.xpath("//label[contains(translate(normalize-space(.), 'Country/Region', 'country/region'), 'country/region')]/following::div[contains(@role,'combobox') or contains(@class,'country') or contains(@class,'region')][1]"),
        By.xpath("//input[@role='combobox' and (contains(translate(@aria-label, 'COUNTRY', 'country'), 'country') or contains(translate(@placeholder, 'COUNTRY', 'country'), 'country'))]"),
        By.xpath("//select[contains(translate(@id, 'COUNTRY', 'country'), 'country') or contains(translate(@name, 'COUNTRY', 'country'), 'country') or contains(translate(@aria-label, 'COUNTRY', 'country'), 'country')]")
      ],
      state: [
        By.name('state'),
        By.name('State'),
        By.css('select[name="State"]'),
        By.xpath("//label[contains(translate(normalize-space(.), 'STATE', 'state'), 'state')]/following::select[1]"),
        By.xpath("//label[contains(translate(normalize-space(.), 'STATE', 'state'), 'state')]/following::input[@role='combobox'][1]"),
        By.xpath("//label[contains(translate(normalize-space(.), 'STATE', 'state'), 'state')]/following::div[contains(@role,'combobox') or contains(@class,'state')][1]"),
        By.xpath("//input[@role='combobox' and (contains(translate(@aria-label, 'STATE', 'state'), 'state') or contains(translate(@placeholder, 'STATE', 'state'), 'state'))]"),
        By.xpath("//select[contains(translate(@id, 'STATE', 'state'), 'state') or contains(translate(@name, 'STATE', 'state'), 'state') or contains(translate(@aria-label, 'STATE', 'state'), 'state')]")
      ]
    };

    return map[f] || [
      By.name(fieldKey),
      By.xpath(`//label[contains(translate(., '${fieldKey.toUpperCase()}', '${fieldKey.toLowerCase()}'), '${fieldKey.toLowerCase()}')]/following::input[1]`),
      By.xpath(`//input[contains(translate(@placeholder, '${fieldKey.toUpperCase()}', '${fieldKey.toLowerCase()}'), '${fieldKey.toLowerCase()}')]`)
    ];
  }

  async findFieldElement(fieldKey) {
    const locators = this.getFieldLocators(fieldKey);

    for (const locator of locators) {
      try {
        const element = await this.driver.findElement(locator);
        if (await element.isDisplayed()) {
          return element;
        }
      } catch (err) {
        // try next locator
      }
    }

    // Fallback: locate <label> text and use associated field
    let labelLocator;
    if (fieldKey === 'country') {
      labelLocator = By.xpath("//label[contains(translate(normalize-space(.), 'COUNTRY', 'country'), 'country')]");
    } else if (fieldKey === 'state') {
      labelLocator = By.xpath("//label[contains(translate(normalize-space(.), 'STATE', 'state'), 'state')]");
    } else {
      const labelText = fieldKey.replace(/([A-Z])/g, ' $1').toLowerCase();
      labelLocator = By.xpath(`//label[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '${labelText}')]`);
    }

    try {
      const labels = await this.driver.findElements(labelLocator);
      for (const label of labels) {
        const forAttr = await label.getAttribute('for');
        if (forAttr) {
          try {
            const target = await this.driver.findElement(By.id(forAttr));
            if (await target.isDisplayed()) {
              return target;
            }
          } catch (ignored) {}
        }

        // Check next-input/select sibling fields from label
        try {
          const sibling = await label.findElement(By.xpath('following::select[1] | following::input[1] | following::div[contains(@role, "combobox")][1]'));
          if (await sibling.isDisplayed()) {
            return sibling;
          }
        } catch (ignored) {}
      }
    } catch (err) {
      // continue to final fallback
    }

    throw new Error(`Field '${fieldKey}' not found with fallback locators`);
  }

  async selectComboboxValue(inputElement, value) {
    const normalizedValue = value.trim();
    await this.driver.executeScript('arguments[0].scrollIntoView({ behavior: "smooth", block: "center" });', inputElement);
    await inputElement.click();
    await this.driver.sleep(250);

    // Clear existing value
    await inputElement.clear();
    await inputElement.sendKeys(Key.chord(Key.CONTROL, 'a'), Key.BACK_SPACE);
    await this.driver.sleep(250);

    await inputElement.sendKeys(normalizedValue);
    await this.driver.sleep(700);

    const ariaControls = await inputElement.getAttribute('aria-controls');
    const searchText = normalizedValue.toLowerCase();

    const trySelectFromListbox = async (listElement) => {
      try {
        const options = await listElement.findElements(By.xpath('.//*[@role="option"] | .//li')); // generic option set
        for (const option of options) {
          try {
            const text = (await option.getText()).trim().toLowerCase();
            if (!text) continue;
            if (text === searchText || text.includes(searchText)) {
              await this.driver.executeScript('arguments[0].scrollIntoView({ behavior: "smooth", block: "center" });', option);
              await this.driver.sleep(150);
              await option.click();
              return true;
            }
          } catch (innerErr) {
            // ignore option read errors
          }
        }
        return false;
      } catch (innerErr) {
        return false;
      }
    };

    if (ariaControls) {
      try {
        const listBox = await this.driver.wait(until.elementLocated(By.id(ariaControls)), 5000);
        await this.driver.wait(until.elementIsVisible(listBox), 5000);

        const selectedOrFound = await trySelectFromListbox(listBox);
        if (selectedOrFound) {
          return true;
        }
      } catch (err) {
        // continue to global listbox search
      }
    }

    // fallback: search any visible listbox or option container by role
    try {
      const roleListBoxes = await this.driver.findElements(By.xpath("//*[@role='listbox' or @role='presentation' or @role='menu']"));
      for (const box of roleListBoxes) {
        if (await box.isDisplayed()) {
          const selectedOrFound = await trySelectFromListbox(box);
          if (selectedOrFound) {
            return true;
          }
        }
      }
    } catch (err) {
      // no listbox found
    }

    try {
      // Try keyboard navigation as a fallback
      await inputElement.sendKeys(Key.ARROW_DOWN);
      await this.driver.sleep(200);
      await inputElement.sendKeys(Key.ENTER);
      await this.driver.sleep(300);

      const selected = await inputElement.getAttribute('value');
      if (selected && selected.toLowerCase().includes(normalizedValue.toLowerCase())) {
        return true;
      }
    } catch (err) {
      // ignore
    }

    return false;
  }

  async selectDropdownValue(fieldKey, value) {
    const locators = this.getFieldLocators(fieldKey);
    let dropdown;

    for (const locator of locators) {
      try {
        const element = await this.driver.findElement(locator);
        if (await element.isDisplayed()) {
          dropdown = element;
          break;
        }
      } catch (err) {
        // try next locator
      }
    }

    if (!dropdown) {
      throw new Error(`Dropdown '${fieldKey}' not found`);
    }

    const tagName = await dropdown.getTagName();

    if (tagName.toLowerCase() === 'select') {
      const normalized = value.trim().toLowerCase();
      const exactOptionLocator = By.xpath(`.//option[translate(normalize-space(text()), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = '${normalized}']`);
      try {
        const option = await dropdown.findElement(exactOptionLocator);
        await option.click();
        return;
      } catch (err) {
        // fallback contains
      }

      const partialOptionLocator = By.xpath(`.//option[contains(translate(normalize-space(text()), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '${normalized}')]`);
      const option = await dropdown.findElement(partialOptionLocator);
      await option.click();
      return;
    }

    // non-select dropdown: could be readonly input or custom UI
    await dropdown.click();
    await this.driver.sleep(500);

    const tagNameLower = tagName.toLowerCase();
    if (tagNameLower === 'input') {
      const success = await this.selectComboboxValue(dropdown, value);
      if (success) {
        return;
      }
    }

    // additional generic attempts for custom dropdowns
    try {
      await dropdown.clear();
    } catch (err) {
      // some custom elements may not support clear
    }

    try {
      await dropdown.sendKeys(value);
      await this.driver.sleep(500);
    } catch (err) {
      // ignore
    }

    const normalizedValue = value.trim().toLowerCase();
    const dropdownOptionLocators = [
      By.xpath(`//li[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '${normalizedValue}')]`),
      By.xpath(`//div[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '${normalizedValue}') and (contains(@role, 'option') or contains(@class, 'option') or contains(@id, 'option'))]`),
      By.xpath(`//span[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '${normalizedValue}') and contains(@class, 'option')]`),
      By.xpath(`//div[@role='option' and contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '${normalizedValue}')]`),
      By.xpath(`//div[contains(@role, 'listbox')]//div[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '${normalizedValue}')]`)
    ];

    for (const locator of dropdownOptionLocators) {
      try {
        const option = await this.driver.wait(until.elementLocated(locator), 5000);
        await this.driver.wait(until.elementIsVisible(option), 5000);
        await this.driver.executeScript('arguments[0].scrollIntoView({ behavior: "smooth", block: "center" });', option);
        await option.click();
        return;
      } catch (err) {
        // try next
      }
    }

    // last resort keyboard navigation
    try {
      await dropdown.sendKeys(Key.ARROW_DOWN, Key.ENTER);
      await this.driver.sleep(300);
      const selectedValue = await dropdown.getAttribute('value');
      if (selectedValue && selectedValue.toLowerCase().includes(normalizedValue)) {
        return;
      }
    } catch (err) {
      // ignore
    }

    throw new Error(`Dropdown option '${value}' not found for '${fieldKey}'`);
  }

  getPlaceOrderButton() {
    return By.xpath("//button[contains(translate(., 'PLACE ORDER', 'place order'), 'place order')] | //button[contains(translate(., 'CONTINUE', 'continue'), 'continue')]");
  }

  getContinueButton() {
    return By.xpath(
      "//button[contains(translate(text(), 'CONTINUE', 'continue'), 'continue')] | " +
      "//input[@type='submit' and contains(translate(@value, 'CONTINUE', 'continue'), 'continue')] | " +
      "//a[contains(translate(text(), 'CONTINUE', 'continue'), 'continue')] | " +
      "//div[contains(@role, 'button') and contains(translate(text(), 'CONTINUE', 'continue'), 'continue')]"
    );
  }

  getBillingSection() {
    return By.xpath("//span[contains(text(), 'Place Order & Pay')] | //span[contains(text(), 'PLACE ORDER & PAY')]");
  }

  getFreeShippingRadio() {
    return By.xpath(
      "//input[@type='radio' and (@id[contains(translate(., 'FREE', 'free'), 'free')] or @name[contains(translate(., 'FREE', 'free'), 'free')])] | " +
      "//label[contains(translate(., 'FREE', 'free'), 'free')]/preceding::input[@type='radio'][1] | " +
      "//label[contains(translate(., 'FREE', 'free'), 'free')]//input[@type='radio']"
    );
  }

  getPlaceOrderPayButton() {
    return By.xpath(
      "//button[contains(translate(., 'PLACE ORDER & PAY', 'place order & pay'), 'place order & pay')] | " +
      "//button[contains(text(), 'Place Order & Pay')] | " +
      "//button[contains(@class, 'pay') or contains(@class, 'order')]"
    );
  }

  // Actions
  async fillCustomerDetails(details) {
    const fields = [
      { key: 'email', value: details.email },
      { key: 'firstName', value: details.firstName },
      { key: 'lastName', value: details.lastName },
      { key: 'phone', value: details.phone },
      { key: 'country', value: details.country },
      { key: 'address1', value: details.address1 },
      { key: 'city', value: details.city },
      { key: 'state', value: details.region },
      { key: 'zip', value: details.zip },
    ];

    for (const field of fields) {
      if (!field.value) {
        console.log(`Skipping empty value for ${field.key}`);
        continue;
      }

      try {
        if (field.key === 'country' || field.key === 'state') {
          await this.selectDropdownValue(field.key, field.value);
          console.log(`Selected ${field.key} = ${field.value}`);
          if (field.key === 'country') {
            await this.driver.sleep(2000); // Wait for region options to update
          }
          continue;
        }

        const element = await this.findFieldElement(field.key);
        if (await element.isDisplayed()) {
          await this.driver.executeScript('arguments[0].scrollIntoView({ behavior: "smooth", block: "center" });', element);
          await this.driver.sleep(300);
          await element.clear();
          await element.sendKeys(Key.chord(Key.CONTROL, 'a'), Key.BACK_SPACE);
          await element.sendKeys(field.value);
          console.log(`Filled ${field.key} with value: ${field.value}`);
          if (field.key === 'zip') {
            // verify zip is set
            const current = await element.getAttribute('value');
            if (current !== field.value) {
              console.warn(`Zip input value mismatch: expected ${field.value}, got ${current}`);
            }
          }
        }
      } catch (err) {
        console.warn(`Unable to fill field '${field.key}': ${err.message}`);
      }
    }
  }

  async placeOrder() {
    const placeOrderBtn = await this.driver.wait(
      until.elementLocated(this.getPlaceOrderButton()),
      10000
    );
    await this.driver.wait(until.elementIsVisible(placeOrderBtn), 10000);
    await this.driver.executeScript('arguments[0].scrollIntoView({ behavior: "smooth", block: "center" });', placeOrderBtn);
    await this.driver.sleep(1000);
    await placeOrderBtn.click();
  }

  async clickContinue() {
    try {
      // Wait for the button to be present and clickable
      const continueBtn = await this.driver.wait(
        until.elementLocated(this.getContinueButton()),
        10000
      );
      
      // Wait for it to be visible
      await this.driver.wait(until.elementIsVisible(continueBtn), 5000);
      
      // Wait for it to be enabled (not disabled)
      await this.driver.wait(
        until.elementIsEnabled(continueBtn),
        5000
      );
      
      // Scroll into view
      await this.driver.executeScript('arguments[0].scrollIntoView({ behavior: "smooth", block: "center" });', continueBtn);
      await this.driver.sleep(1000);
      
      // Try to click using JavaScript if regular click fails
      try {
        await continueBtn.click();
        console.log('Continue button clicked successfully');
      } catch (clickErr) {
        console.log('Regular click failed, trying JavaScript click');
        await this.driver.executeScript('arguments[0].click();', continueBtn);
        console.log('Continue button clicked with JavaScript');
      }
      
      await this.driver.sleep(2000);
    } catch (err) {
      console.warn(`Unable to click continue button: ${err.message}`);
      // Try to find all continue buttons and log them for debugging
      try {
        const allContinueBtns = await this.driver.findElements(By.xpath("//button[contains(text(), 'Continue')] | //input[contains(@value, 'Continue')]"));
        console.log(`Found ${allContinueBtns.length} continue buttons on page`);
        for (let i = 0; i < allContinueBtns.length; i++) {
          const btn = allContinueBtns[i];
          const isVisible = await btn.isDisplayed();
          const isEnabled = await btn.isEnabled();
          const text = await btn.getText();
          console.log(`Continue button ${i+1}: visible=${isVisible}, enabled=${isEnabled}, text="${text}"`);
        }
      } catch (debugErr) {
        console.warn(`Debug info failed: ${debugErr.message}`);
      }
      throw err; // Re-throw to fail the test
    }
  }

  async waitForPageTransition() {
    try {
      // Wait for any loading indicators to disappear
      await this.driver.sleep(1000);
      
      // Wait for page to stabilize (no more network requests)
      await this.driver.wait(async () => {
        const readyState = await this.driver.executeScript('return document.readyState');
        return readyState === 'complete';
      }, 10000);
      
      console.log('Page transition completed');
    } catch (err) {
      console.warn(`Page transition wait failed: ${err.message}`);
    }
  }

  async waitForCheckoutPageLoad() {
    const title = await this.driver.wait(
      until.elementLocated(this.getCheckoutTitle()),
      10000
    );
    await this.driver.wait(until.elementIsVisible(title), 10000);
  }

  async isCheckoutPageLoaded() {
    try {
      const title = await this.driver.findElement(this.getCheckoutTitle());
      return await title.isDisplayed();
    } catch (err) {
      return false;
    }
  }

  async areCustomerFieldsVisible() {
    try {
      const emailField = await this.driver.findElement(this.getEmailField());
      return await emailField.isDisplayed();
    } catch (err) {
      return false;
    }
  }

  async selectFreeShipping() {
    try {
      const freeShippingRadio = await this.driver.findElement(this.getFreeShippingRadio());
      const isSelected = await freeShippingRadio.isSelected();
      
      if (!isSelected) {
        await this.driver.executeScript('arguments[0].scrollIntoView({ behavior: "smooth", block: "center" });', freeShippingRadio);
        await this.driver.sleep(500);
        await freeShippingRadio.click();
        console.log('Free shipping selected');
        await this.driver.sleep(1000);
      } else {
        console.log('Free shipping was already selected');
      }
    } catch (err) {
      console.warn(`Unable to select free shipping: ${err.message}`);
      throw err;
    }
  }

  async clickPlaceOrderPay() {
    try {
      const placeOrderPayBtn = await this.driver.wait(
        until.elementLocated(this.getPlaceOrderPayButton()),
        10000
      );
      await this.driver.wait(until.elementIsVisible(placeOrderPayBtn), 10000);
      await this.driver.executeScript('arguments[0].scrollIntoView({ behavior: "smooth", block: "center" });', placeOrderPayBtn);
      await this.driver.sleep(1000);
      await placeOrderPayBtn.click();
      console.log('Clicked Place Order & Pay button');
      await this.driver.sleep(3000);
    } catch (err) {
      console.warn(`Unable to click Place Order & Pay button: ${err.message}`);
      throw err;
    }
  }
}

module.exports = CheckoutPage;
