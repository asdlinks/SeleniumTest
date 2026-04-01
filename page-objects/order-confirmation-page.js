const { By, until } = require('selenium-webdriver');

class OrderConfirmationPage {
  constructor(driver) {
    this.driver = driver;
  }

  // Locators
  getThankYouMessage() {
    return By.xpath(
      "//h1[contains(text(), 'Thank You')] | //h2[contains(text(), 'Thank You')] | //span[contains(text(), 'Thank You')] | " +
      "//h1[contains(text(), 'Order')] | //div[contains(text(), 'Order Confirmed')]"
    );
  }

  getOrderNumber() {
    return By.xpath(
      "//span[contains(text(), 'Order')] | //div[contains(@class, 'order-number')] | //p[contains(text(), '#')]"
    );
  }

  getConfirmationDetails() {
    return By.xpath(
      "//div[contains(@class, 'confirmation')] | //section[contains(@class, 'confirmation')] | " +
      "//div[contains(@class, 'order-details')]"
    );
  }

  getContinueShoppingButton() {
    return By.xpath(
      "//button[contains(text(), 'Continue')] | //a[contains(text(), 'Shop')] | //button[contains(text(), 'Back')]"
    );
  }

  getOrderStatus() {
    return By.xpath("//span[contains(@class, 'status')] | //div[contains(@class, 'status')]");
  }

  getEmailConfirmation() {
    return By.xpath("//span[contains(text(), 'email')] | //p[contains(text(), 'email')] | //text()[contains(translate(., 'CONFIRMATION EMAIL', 'confirmation email'), 'confirmation email')]/..");
  }

  getFirstNameText() {
    return By.xpath(
      "//span[contains(text(), 'John')] | //p[contains(text(), 'John')] | " +
      "//div[contains(text(), 'first') or contains(text(), 'First')]"
    );
  }

  getLastNameText() {
    return By.xpath(
      "//span[contains(text(), 'Doe')] | //p[contains(text(), 'Doe')] | " +
      "//div[contains(text(), 'last') or contains(text(), 'Last')]"
    );
  }

  getConfirmationEmailMessage() {
    return By.xpath(
      "//text()[contains(translate(., 'CONFIRMATION EMAIL SOON', 'confirmation email soon'), 'confirmation email soon')]/.. | " +
      "//p[contains(translate(., 'CONFIRMATION', 'confirmation'), 'confirmation') and contains(translate(., 'EMAIL', 'email'), 'email')] | " +
      "//div[contains(translate(., 'CONFIRMATION', 'confirmation'), 'confirmation')]"
    );
  }

  // Actions
  async waitForConfirmationPageLoad() {
    const thankYouMsg = await this.driver.wait(
      until.elementLocated(this.getThankYouMessage()),
      15000
    );
    await this.driver.wait(until.elementIsVisible(thankYouMsg), 10000);
  }

  async getThankYouText() {
    const message = await this.driver.wait(
      until.elementLocated(this.getThankYouMessage()),
      10000
    );
    return await message.getText();
  }

  async isThankYouMessageVisible() {
    try {
      const message = await this.driver.findElement(this.getThankYouMessage());
      return await message.isDisplayed();
    } catch (err) {
      return false;
    }
  }

  async getOrderDetails() {
    try {
      const details = await this.driver.findElement(this.getConfirmationDetails());
      return await details.getText();
    } catch (err) {
      return 'Order details not found';
    }
  }

  async extractOrderNumber() {
    try {
      await this.driver.wait(until.elementLocated(this.getOrderNumber()), 10000);
      const pageText = await this.driver.executeScript('return document.body.innerText');
      
      // Try to extract order number from the page text
      // Look for patterns like "Order #123456" or "Order Number: 123456"
      const orderPatterns = [
        /Order\s*#?(\d+)/i,
        /Order\s*Number[:\s]+(\d+)/i,
        /Ref\s*#?(\d+)/i,
        /#(\d{6,})/
      ];
      
      for (const pattern of orderPatterns) {
        const match = pageText.match(pattern);
        if (match && match[1]) {
          console.log(`Extracted Order Number: ${match[1]}`);
          return match[1];
        }
      }
      
      // Fallback to element text
      const orderElement = await this.driver.findElement(this.getOrderNumber());
      const orderText = await orderElement.getText();
      console.log(`Order Element Text: ${orderText}`);
      return orderText;
    } catch (err) {
      console.warn(`Unable to extract order number: ${err.message}`);
      return 'Order number not found';
    }
  }

  async isOrderConfirmed() {
    try {
      const thankYouMsg = await this.driver.findElement(this.getThankYouMessage());
      const isVisible = await thankYouMsg.isDisplayed();
      const text = await thankYouMsg.getText();
      return isVisible && (text.toLowerCase().includes('thank you') || text.toLowerCase().includes('confirmed'));
    } catch (err) {
      return false;
    }
  }

  async clickContinueShopping() {
    try {
      const continueBtn = await this.driver.findElement(this.getContinueShoppingButton());
      if (await continueBtn.isDisplayed()) {
        await continueBtn.click();
      }
    } catch (err) {
      // Button might not exist
    }
  }

  async validateCustomerNameOnConfirmation(firstName, lastName) {
    try {
      const pageText = await this.driver.executeScript('return document.body.innerText');
      const hasFirstName = pageText.includes(firstName);
      const hasLastName = pageText.includes(lastName);
      
      console.log(`First Name '${firstName}' visible: ${hasFirstName}`);
      console.log(`Last Name '${lastName}' visible: ${hasLastName}`);
      
      return hasFirstName && hasLastName;
    } catch (err) {
      console.warn(`Error validating customer names: ${err.message}`);
      return false;
    }
  }

  async validateEmailConfirmationMessage() {
    try {
      const pageText = await this.driver.executeScript('return document.body.innerText');
      const hasEmailMessage = pageText.toLowerCase().includes('confirmation email') || 
                              pageText.toLowerCase().includes("you'll receive");
      
      console.log(`Email confirmation message visible: ${hasEmailMessage}`);
      if (hasEmailMessage) {
        const emailMatch = pageText.match(/you['']ll\s+receive[^.!?]*email[^.!?]*[.!?]/i);
        if (emailMatch) {
          console.log(`Email message: ${emailMatch[0]}`);
        }
      }
      
      return hasEmailMessage;
    } catch (err) {
      console.warn(`Error validating email confirmation message: ${err.message}`);
      return false;
    }
  }

  async getStoredOrderNumber() {
    // This retrieves a previously extracted order number
    try {
      return global.storedOrderNumber || 'Order number not stored';
    } catch (err) {
      return 'Order number not found';
    }
  }
}

module.exports = OrderConfirmationPage;
