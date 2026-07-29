/**
 * Example step definitions updated to use the centralized config module
 * instead of hardcoded production URLs.
 */
const config = require('../../config/env');

module.exports = function () {
    this.Given(/^I navigate to the home page$/, function () {
        return driver.get(config.baseUrl);
    });

    this.Given(/^I navigate to the login page$/, function () {
        return driver.get(config.paths.login);
    });

    this.Given(/^I navigate to the cart page$/, function () {
        return driver.get(config.paths.cart);
    });

    this.Given(/^I navigate to the checkout page$/, function () {
        return driver.get(config.paths.checkout);
    });
};
