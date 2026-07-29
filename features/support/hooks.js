/**
 * Test bootstrap/support hooks.
 * Loads environment variables via dotenv and wires in the centralized config.
 */
require('dotenv').config();

const config = require('../../config/env');

module.exports = function () {
    this.Before(function () {
        global.config = config;
    });

    this.After(function () {
        // teardown logic if required
    });
};
