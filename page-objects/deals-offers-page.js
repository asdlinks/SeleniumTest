const { By, Key, until } = require('selenium-webdriver');

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

  getAllProductsLink() {
    return By.xpath("//aside//a[contains(normalize-space(.), 'All Products')]");
  }

  getLoadMoreButton() {
    return By.xpath("//button[contains(normalize-space(.), 'Load More')]");
  }

  getPriceSlider(label) {
    return By.css(`[role="slider"][aria-label="${label}"]`);
  }

  getProductCards() {
    return By.css('main a[href*="/product-page/"]');
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

  async clickAllProductsLink() {
    const link = await this.driver.wait(until.elementLocated(this.getAllProductsLink()), 10000);
    await this.driver.wait(until.elementIsVisible(link), 10000);
    await this.driver.executeScript('arguments[0].scrollIntoView({ block: "center" });', link);
    await this.driver.sleep(500);
    await this.driver.executeScript('arguments[0].click();', link);
  }

  async getProductCardCount() {
    const cards = await this.driver.findElements(this.getProductCards());
    return cards.length;
  }

  async clickLoadMore() {
    const button = await this.driver.wait(until.elementLocated(this.getLoadMoreButton()), 10000);
    await this.driver.wait(until.elementIsVisible(button), 10000);
    await this.driver.executeScript('arguments[0].scrollIntoView({ block: "center" });', button);
    await this.driver.sleep(500);
    await this.driver.executeScript('arguments[0].click();', button);
    await this.driver.sleep(1500);
  }

  async setPriceRange(maxValue, minValue = 0) {
    const minSlider = await this.driver.findElement(this.getPriceSlider('Minimum price'));
    const maxSlider = await this.driver.findElement(this.getPriceSlider('Maximum price'));

    const moveSliderToValue = async (slider, targetValue) => {
      await this.driver.executeScript('arguments[0].focus();', slider);
      await this.driver.sleep(200);

      let currentValue = Number(await slider.getAttribute('aria-valuenow'));
      const min = Number(await slider.getAttribute('aria-valuemin'));
      const max = Number(await slider.getAttribute('aria-valuemax'));
      const safeTarget = Math.min(Math.max(targetValue, min), max);

      while (currentValue < safeTarget) {
        await slider.sendKeys(Key.ARROW_RIGHT);
        currentValue += 1;
      }

      while (currentValue > safeTarget) {
        await slider.sendKeys(Key.ARROW_LEFT);
        currentValue -= 1;
      }

      await this.driver.sleep(500);
    };

    await moveSliderToValue(minSlider, minValue);
    await moveSliderToValue(maxSlider, maxValue);
  }

  async getProductPrices() {
    const bodyText = await this.driver.findElement(By.css('body')).getText();
    return bodyText.match(/\$\d+(?:\.\d{2})?/g) || [];
  }

  async getFilterRangeText() {
    return this.driver.executeScript(`
      return document.body.innerText;
    `);
  }
}

module.exports = DealsAndOffersPage;
