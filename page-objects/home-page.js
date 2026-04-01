const { By } = require('selenium-webdriver');

class HomePage {
  constructor(driver) {
    this.driver = driver;
  }

  // Locators
  getDealsOffersLink() {
    return By.xpath("//a[contains(text(), 'Deals') or contains(text(), 'Offers')]");
  }

  getNavDealsOffersButton() {
    return By.xpath("//span[contains(text(), 'Deals')] | //span[contains(text(), 'Offers')]");
  }

  // Common elements
  getSiteLogo() {
    return By.css('img[alt*="logo"], .logo');
  }

  // Actions
  async navigateToHome(url) {
    await this.driver.get(url);
  }

  async clickDealsAndOffers() {
    try {
      const dealsLink = await this.driver.findElement(this.getDealsOffersLink());
      await dealsLink.click();
    } catch (err) {
      // Try alternative selector
      const navButton = await this.driver.findElement(this.getNavDealsOffersButton());
      await navButton.click();
    }
  }

  async waitForPageLoad() {
    await this.driver.wait(async () => {
      const readyState = await this.driver.executeScript('return document.readyState');
      return readyState === 'complete';
    }, 15000);
  }
}

module.exports = HomePage;
