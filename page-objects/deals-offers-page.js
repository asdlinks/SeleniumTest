const { By, until } = require('selenium-webdriver');

class DealsAndOffersPage {
  constructor(driver) {
    this.driver = driver;
  }

  // Locators
  getHardwareCategoryButton() {
    return By.xpath("//a[contains(text(), 'Hardware')] | //span[contains(text(), 'Hardware')] | //button[contains(text(), 'Hardware')]");
  }

  getCategoryLink(categoryName) {
    return By.xpath(`//a[contains(text(), '${categoryName}')] | //span[contains(text(), '${categoryName}')]`);
  }

  getPageTitle() {
    return By.xpath("//h1[contains(text(), 'Deals')] | //h1[contains(text(), 'Offers')]");
  }

  // Actions
  async selectCategory(categoryName) {
    const categoryLocator = this.getCategoryLink(categoryName);
    const element = await this.driver.wait(
      until.elementLocated(categoryLocator),
      10000
    );
    await this.driver.wait(until.elementIsVisible(element), 10000);
    await this.driver.executeScript('arguments[0].scrollIntoView({ behavior: "smooth", block: "center" });', element);
    await this.driver.sleep(1000);
    await element.click();
  }

  async waitForPageLoad() {
    const title = await this.driver.wait(
      until.elementLocated(this.getPageTitle()),
      10000
    );
    await this.driver.wait(until.elementIsVisible(title), 10000);
  }

  async isDealsOffersPageLoaded() {
    try {
      const title = await this.driver.findElement(this.getPageTitle());
      return await title.isDisplayed();
    } catch (err) {
      return false;
    }
  }
}

module.exports = DealsAndOffersPage;
