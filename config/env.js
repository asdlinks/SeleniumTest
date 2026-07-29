/**
 * Centralized environment/config module.
 * Resolves the base URL from the BASE_URL environment variable,
 * falling back to a safe staging default instead of production.
 */
require('dotenv').config();

const DEFAULT_BASE_URL = 'https://staging.shopbricks.com';

const baseUrl = process.env.BASE_URL || DEFAULT_BASE_URL;

module.exports = {
    baseUrl,
    paths: {
        home: `${baseUrl}/home`,
        login: `${baseUrl}/login`,
        cart: `${baseUrl}/cart`,
        checkout: `${baseUrl}/checkout`
    }
};
