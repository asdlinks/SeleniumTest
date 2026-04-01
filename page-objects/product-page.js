const { By, until } = require('selenium-webdriver');

class ProductPage {
  constructor(driver) {
    this.driver = driver;
  }

  // Locators
  getProductTitle() {
    return By.xpath("//h1 | //h2[contains(@class, 'product-title')]");
  }

  getAddToCartButton() {
    return By.xpath("//span[text()='Add to Cart'] | //button[contains(text(), 'Add to Cart')] | //button[contains(text(), 'ADD TO CART')]");
  }

  getPrice() {
    return By.xpath("//span[contains(@class, 'price')] | //div[contains(text(), '$')]");
  }

  getProductDescription() {
    return By.xpath("//div[contains(@class, 'description')] | //p[contains(@class, 'description')]");
  }

  getAddToCartSuccessMessage() {
    return By.xpath("//div[contains(text(), 'added')] | //span[contains(text(), 'Cart')] | //div[contains(@class, 'success')]");
  }

  // Actions
  async addToCart() {
    const addToCartBtn = await this.driver.wait(
      until.elementLocated(this.getAddToCartButton()),
      10000
    );
    await this.driver.wait(until.elementIsVisible(addToCartBtn), 10000);
    await this.driver.executeScript('arguments[0].scrollIntoView({ behavior: "smooth", block: "center" });', addToCartBtn);
    await this.driver.sleep(2000);
    await addToCartBtn.click();
  }

  async waitForProductPageLoad() {
    const title = await this.driver.wait(
      until.elementLocated(this.getProductTitle()),
      10000
    );
    await this.driver.wait(until.elementIsVisible(title), 10000);
  }

  async isAddToCartVisible() {
    try {
      const button = await this.driver.findElement(this.getAddToCartButton());
      return await button.isDisplayed();
    } catch (err) {
      return false;
    }
  }

  async getProductName() {
    const title = await this.driver.findElement(this.getProductTitle());
    return await title.getText();
  }

  async getProductPrice() {
    try {
      const price = await this.driver.findElement(this.getPrice());
      return await price.getText();
    } catch (err) {
      return 'Price not found';
    }
  }
}

module.exports = ProductPage;
