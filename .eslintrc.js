/**
 * ESLint configuration including a rule/check that fails the build
 * if a raw production-like URL literal is committed instead of
 * going through the centralized config module.
 */
module.exports = {
    extends: 'airbnb-base',
    env: {
        es6: false,
        browser: true
    },
    globals: {
        selenium: true,
        helpers: true,
        page: true,
        driver: true,
        until: true,
        by: true,
        expect: true,
        Promise: true,
        browserName: true,
        DEFAULT_TIMEOUT: true
    },
    rules: {
        'no-restricted-syntax': [
            'error',
            {
                selector: 'Literal[value=/shopbricks\\.com/]',
                message: 'Do not hardcode shopbricks.com URLs. Use config.baseUrl from config/env.js instead.'
            }
        ]
    }
};
