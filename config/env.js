/**
 * Centralized environment configuration for the Shop Bricks BDD Selenium test suite.
 *
 * Resolves the base URL from the BASE_URL environment variable, falling back
 * to a safe staging default rather than the live production site.
 */

require('dotenv').config();

const baseUrl = process.env.BASE_URL || 'https://staging.shopbricks.com';

module.exports = {
    baseUrl,
    path: function (relativePath) {
        const trimmedBase = baseUrl.replace(/\/+$/, '');
        const trimmedPath = String(relativePath || '').replace(/^\/+/, '');
        return trimmedPath ? `${trimmedBase}/${trimmedPath}` : trimmedBase;
    }
};
