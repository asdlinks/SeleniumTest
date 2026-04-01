const { By, until } = require('selenium-webdriver');

class CartPage {
  constructor(driver) {
    this.driver = driver;
  }

  // Locators
  getCartTitle() {
    return By.xpath("//h1[contains(text(), 'Cart')] | //h1[contains(text(), 'Shopping')] | //span[contains(text(), 'Cart')]");
  }

  getCheckoutButton() {
    return By.xpath("//button[contains(text(), 'Checkout')] | //a[contains(text(), 'Checkout')] | //span[text()='Checkout']");
  }

  getContinueShoppingButton() {
    return By.xpath("//button[contains(text(), 'Continue')] | //a[contains(text(), 'Continue')]");
  }

  getCartItemCount() {
    return By.xpath("//div[contains(@class, 'cart-count')] | //span[contains(@class, 'item-count')]");
  }

  getProductInCart(productName) {
    return By.xpath(`//div[contains(text(), '${productName}')] | //span[contains(text(), '${productName}')]`);
  }

  getCartSubtotal() {
    return By.xpath("//span[contains(text(), 'Subtotal')] | //div[contains(text(), 'Subtotal')]");
  }

  getRemoveItemButton() {
    return By.xpath("//button[contains(text(), 'Remove')] | //a[contains(text(), 'Remove')]");
  }

  // Actions
  async proceedToCheckout() {
    // Wait for checkout button to be clickable
    const checkoutBtn = await this.driver.wait(
      until.elementLocated(this.getCheckoutButton()),
      10000
    );
    await this.driver.wait(until.elementIsVisible(checkoutBtn), 10000);
    await this.driver.wait(until.elementIsEnabled(checkoutBtn), 10000);
    await this.driver.executeScript('arguments[0].scrollIntoView({ behavior: "smooth", block: "center" });', checkoutBtn);
    await this.driver.sleep(1000);
    await checkoutBtn.click();
  }

  async waitForCartPageLoad() {
    const title = await this.driver.wait(
      until.elementLocated(this.getCartTitle()),
      10000
    );
    await this.driver.wait(until.elementIsVisible(title), 10000);
  }

  async isProductInCart(productName) {
    try {
      const product = await this.driver.findElement(this.getProductInCart(productName));
      return await product.isDisplayed();
    } catch (err) {
      return false;
    }
  }

  async getItemCount() {
    try {
      const countElement = await this.driver.findElement(this.getCartItemCount());
      return await countElement.getText();
    } catch (err) {
      return '0';
    }
  }

  async isCheckoutButtonVisible() {
    try {
      const button = await this.driver.findElement(this.getCheckoutButton());
      return await button.isDisplayed();
    } catch (err) {
      return false;
    }
  }
}

module.exports = CartPage;
