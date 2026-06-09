const path = require('path');
const { Builder } = require('selenium-webdriver');
const chrome = require('selenium-webdriver/chrome');
const chromedriver = require('chromedriver');
const HomePage = require('../page-objects/home-page');
const homePageData = require('../shared-objects/home-page-data');

async function runValidation() {
    process.env.PATH = `${process.env.PATH}${path.delimiter}${path.dirname(chromedriver.path)}`;
    let driver;
    let exitCode = 0;

    try {
        const options = new chrome.Options();
        options.addArguments('start-maximized', 'disable-extensions');

        driver = await new Builder()
            .forBrowser('chrome')
            .setChromeOptions(options)
            .build();

        const homePage = new HomePage(driver);
        const expected = homePageData.homepage;

        await homePage.navigateToHome('https://www.shopbricks.co/');
        await homePage.waitForPageLoad();

        const actualHeading = await homePage.getTextFromLocator(await homePage.getShopByCategoryHeading());
        console.log(`Actual heading: ${actualHeading}`);

        if (actualHeading !== expected.shopByCategoryHeading) {
            throw new Error(`Expected heading '${expected.shopByCategoryHeading}', but found '${actualHeading}'`);
        }

        const shopByCategoryItems = await homePage.getItemsUnderHeading(expected.shopByCategoryHeading);
        console.log(`Items under '${expected.shopByCategoryHeading}':`, shopByCategoryItems);

        if (expected.shopByCategoryItems && expected.shopByCategoryItems.length > 0) {
            expected.shopByCategoryItems.forEach((item) => {
                if (!shopByCategoryItems.includes(item)) {
                    throw new Error(`Expected item '${item}' under '${expected.shopByCategoryHeading}'`);
                }
            });
        }

        for (const category of expected.categories) {
            const locator = await homePage.getSectionItemLocator(category);
            const visible = await homePage.isLocatorVisible(locator);
            console.log(`Category '${category}' visible: ${visible}`);
            if (!visible) {
                throw new Error(`Expected category '${category}' to be visible on homepage`);
            }

            const categoryItems = await homePage.getItemsUnderHeading(category);
            console.log(`Items under '${category}':`, categoryItems);

            const expectedKey = category === 'Shop Power Tools' ? 'shopPowerToolsItems' : category === 'Shop Hardware' ? 'shopHardwareItems' : null;
            if (expectedKey && expected[expectedKey] && expected[expectedKey].length > 0) {
                expected[expectedKey].forEach((item) => {
                    if (!categoryItems.includes(item)) {
                        throw new Error(`Expected item '${item}' under '${category}'`);
                    }
                });
            }
        }

        const actualTrustedHeading = await homePage.getTextFromLocator(await homePage.getTrustedBrandsHeading());
        console.log(`Actual trusted brands heading: ${actualTrustedHeading}`);

        if (actualTrustedHeading !== expected.trustedBrandsHeading) {
            throw new Error(`Expected heading '${expected.trustedBrandsHeading}', but found '${actualTrustedHeading}'`);
        }

        const trustedBrandItems = await homePage.getItemsUnderHeading(expected.trustedBrandsHeading);
        console.log(`Items under '${expected.trustedBrandsHeading}':`, trustedBrandItems);

        if (expected.trustedBrandItems && expected.trustedBrandItems.length > 0) {
            expected.trustedBrandItems.forEach((item) => {
                if (!trustedBrandItems.includes(item)) {
                    throw new Error(`Expected trusted brand '${item}' under '${expected.trustedBrandsHeading}'`);
                }
            });
        }

        console.log('Homepage validation passed: expected values match the Shopbricks main page.');
    } catch (error) {
        console.error('Homepage validation failed:', error.message);
        exitCode = 1;
    } finally {
        if (driver) {
            await driver.quit();
        }
        process.exit(exitCode);
    }
}

runValidation();
