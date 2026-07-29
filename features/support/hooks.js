/**
 * Cucumber hooks / test bootstrap.
 *
 * Loads environment configuration (via dotenv, wired through config/env.js)
 * so that BASE_URL can be set locally (.env) or via CI secrets/variables.
 */

require('dotenv').config();

const config = require('../../config/env');
const { Before, After } = require('cucumber');

Before(function () {
    // expose resolved base url to the world for use in step definitions
    this.baseUrl = config.baseUrl;
});

After(function () {
    // hook reserved for teardown logic
});
