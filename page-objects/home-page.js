const { By, until } = require('selenium-webdriver');

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

  async getShopByCategoryHeading() {
    return By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(.), 'Shop By Category')]");
  }

  async getSectionItemLocator(itemText) {
    return By.xpath(`//a[contains(normalize-space(.), '${itemText}')] | //button[contains(normalize-space(.), '${itemText}')] | //span[contains(normalize-space(.), '${itemText}')] | //div[contains(normalize-space(.), '${itemText}')]`);
  }

  async getTrustedBrandsHeading() {
    return By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(.), 'Our Trusted Brands')]");
  }

  async getItemsUnderHeading(headingText) {
    return await this.driver.executeScript(`
      const heading = Array.from(document.querySelectorAll('h1,h2,h3')).find(el => el.textContent.trim().includes(arguments[0]));
      if (!heading) {
        return [];
      }

      const section = heading.closest('section,div,aside') || heading.parentElement;
      if (!section) {
        return [];
      }

      const itemNodes = Array.from(section.querySelectorAll('a,button,span,li'));
      const cleaned = itemNodes
        .map(el => el.textContent.trim())
        .filter(text => text && text !== arguments[0]);
      return Array.from(new Set(cleaned));
    `, headingText);
  }

  async getTextFromLocator(locator) {
    const element = await this.driver.wait(until.elementLocated(locator), 10000);
    await this.driver.wait(until.elementIsVisible(element), 10000);
    return (await element.getText()).trim();
  }

  async isLocatorVisible(locator) {
    try {
      const element = await this.driver.findElement(locator);
      return await element.isDisplayed();
    } catch (err) {
      return false;
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
