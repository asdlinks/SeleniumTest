const { By, until } = require('selenium-webdriver');

class CategoryPage {
  constructor(driver) {
    this.driver = driver;
  }

  // Locators
  getCategoryTitle() {
    return By.xpath("//h1[contains(text(), 'Hardware')] | //h2[contains(text(), 'Hardware')]");
  }

  getProductByName(productName) {
    return By.xpath(
      `//a[contains(text(), '${productName}')] | //div[contains(text(), '${productName}')] | //span[contains(text(), '${productName}')]`
    );
  }

  getProductLink(productName) {
    return By.xpath(`//a[contains(@href, '/product')] | //a[contains(text(), '${productName}')]`);
  }

  getFirstProduct() {
    return By.css('a[href*="/product"], .product-card a, .product-item a');
  }

  // Actions
  async selectProduct(productName) {
    const productLocator = this.getProductByName(productName);
    try {
      const element = await this.driver.wait(
        until.elementLocated(productLocator),
        10000
      );
      await this.driver.wait(until.elementIsVisible(element), 10000);
      await this.driver.executeScript('arguments[0].scrollIntoView({ behavior: "smooth", block: "center" });', element);
      await this.driver.sleep(500);
      await element.click();
    } catch (err) {
      // If product name not found, try to find a link that leads to the product
      const linkLocator = this.getProductLink(productName);
      const link = await this.driver.wait(until.elementLocated(linkLocator), 10000);
      await this.driver.wait(until.elementIsVisible(link), 10000);
      await link.click();
    }
  }

  async waitForCategoryPageLoad() {
    const title = await this.driver.wait(
      until.elementLocated(this.getCategoryTitle()),
      10000
    );
    await this.driver.wait(until.elementIsVisible(title), 10000);
  }

  async isCategoryPageLoaded() {
    try {
      const title = await this.driver.findElement(this.getCategoryTitle());
      return await title.isDisplayed();
    } catch (err) {
      return false;
    }
  }
}

module.exports = CategoryPage;
