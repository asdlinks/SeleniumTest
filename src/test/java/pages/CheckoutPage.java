package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import support.Helpers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CheckoutPage extends BasePage {

    public CheckoutPage(WebDriver driver) {
        super(driver);
    }

    /** The customer details filled into the checkout form. */
    public static class CustomerDetails {
        public String email = "";
        public String firstName = "";
        public String lastName = "";
        public String address1 = "";
        public String city = "";
        public String country = "";
        public String region = "";
        public String zip = "";
        public String phone = "";
    }

    public By getCheckoutTitle() {
        return By.xpath("//h1[contains(translate(., 'CHECKOUT', 'checkout'), 'checkout')]"
                + " | //h2[contains(translate(., 'CHECKOUT', 'checkout'), 'checkout')]"
                + " | //span[contains(translate(., 'CHECKOUT', 'checkout'), 'checkout')]");
    }

    public By getPlaceOrderButton() {
        return By.xpath("//button[contains(translate(., 'PLACE ORDER', 'place order'), 'place order')]"
                + " | //button[contains(translate(., 'CONTINUE', 'continue'), 'continue')]");
    }

    /** Final "Place Order & Pay" button shown once the whole checkout has been reviewed. */
    public By getFinalPlaceOrderButton() {
        return By.xpath("//button[contains(translate(., 'PLACE ORDER', 'place order'), 'place order')]");
    }

    /** A "Continue" button that advances to the next checkout accordion section. */
    public By getContinueOnlyButton() {
        return By.xpath("//button[contains(translate(., 'CONTINUE', 'continue'), 'continue')"
                + " and not(contains(translate(., 'PLACE ORDER', 'place order'), 'place order'))]");
    }

    /**
     * Waits for the customer details form (the email input) to render before
     * any field is filled in - the checkout page loads its form asynchronously
     * after navigation, so filling immediately can land before the fields
     * exist.
     */
    public void waitForCustomerDetailsForm() {
        waitFor(By.cssSelector("input[type=\"email\"]"), Duration.ofSeconds(20));
    }

    public List<By> getFieldLocators(String fieldKey) {
        String f = fieldKey.toLowerCase();

        Map<String, List<By>> map = new LinkedHashMap<>();

        map.put("email", Arrays.asList(
                By.name("email"),
                By.name("Email"),
                By.cssSelector("input[type=\"email\"]"),
                By.xpath("//label[contains(translate(., 'EMAIL', 'email'), 'email')]/following::input[1]"),
                By.xpath("//input[contains(translate(@placeholder, 'EMAIL', 'email'), 'email')]")));

        map.put("firstname", Arrays.asList(
                By.name("firstName"),
                By.name("first_name"),
                By.name("firstname"),
                By.name("First name"),
                By.xpath("//label[contains(translate(., 'FIRST', 'first'), 'first') and contains(translate(., 'NAME', 'name'), 'name')]/following::input[1]"),
                By.xpath("//input[contains(translate(@placeholder, 'FIRST', 'first'), 'first')]")));

        map.put("lastname", Arrays.asList(
                By.name("lastName"),
                By.name("last_name"),
                By.name("lastname"),
                By.name("Last name"),
                By.xpath("//label[contains(translate(., 'LAST', 'last'), 'last') and contains(translate(., 'NAME', 'name'), 'name')]/following::input[1]"),
                By.xpath("//input[contains(translate(@placeholder, 'LAST', 'last'), 'last')]")));

        map.put("address1", Arrays.asList(
                By.name("address1"),
                By.name("address"),
                By.name("Address"),
                By.xpath("//label[contains(translate(., 'ADDRESS', 'address'), 'address')]/following::input[1]"),
                By.xpath("//input[contains(translate(@placeholder, 'ADDRESS', 'address'), 'address')]")));

        map.put("city", Arrays.asList(
                By.name("city"),
                By.name("City"),
                By.xpath("//label[contains(translate(., 'CITY', 'city'), 'city')]/following::input[1]"),
                By.xpath("//input[contains(translate(@placeholder, 'CITY', 'city'), 'city')]")));

        map.put("zip", Arrays.asList(
                By.name("zip"),
                By.name("postalCode"),
                By.name("Zip"),
                By.name("PostalCode"),
                By.name("postal_code"),
                By.xpath("//label[contains(translate(., 'ZIP', 'zip'), 'zip') or contains(translate(., 'POSTAL', 'postal'), 'postal')]/following::input[1]"),
                By.xpath("//input[contains(translate(@placeholder, 'ZIP', 'zip'), 'zip') or contains(translate(@placeholder, 'POSTAL', 'postal'), 'postal')]")));

        map.put("phone", Arrays.asList(
                By.name("phone"),
                By.name("Phone"),
                By.name("mobile"),
                By.xpath("//label[contains(translate(., 'PHONE', 'phone'), 'phone')]/following::input[1]"),
                By.xpath("//input[contains(translate(@placeholder, 'PHONE', 'phone'), 'phone')]")));

        map.put("country", Arrays.asList(
                By.name("country"),
                By.name("Country"),
                By.cssSelector("select[name=\"country\"]"),
                By.xpath("//label[contains(translate(normalize-space(.), 'Country/Region', 'country/region'), 'country/region')]/following::select[1]"),
                By.xpath("//label[contains(translate(normalize-space(.), 'Country/Region', 'country/region'), 'country/region')]/following::input[@role='combobox'][1]"),
                By.xpath("//label[contains(translate(normalize-space(.), 'Country/Region', 'country/region'), 'country/region')]/following::div[contains(@role,'combobox') or contains(@class,'country') or contains(@class,'region')][1]"),
                By.xpath("//input[@role='combobox' and (contains(translate(@aria-label, 'COUNTRY', 'country'), 'country') or contains(translate(@placeholder, 'COUNTRY', 'country'), 'country'))]"),
                By.xpath("//select[contains(translate(@id, 'COUNTRY', 'country'), 'country') or contains(translate(@name, 'COUNTRY', 'country'), 'country') or contains(translate(@aria-label, 'COUNTRY', 'country'), 'country')]")));

        map.put("state", Arrays.asList(
                By.name("state"),
                By.name("State"),
                By.cssSelector("select[name=\"State\"]"),
                By.xpath("//label[contains(translate(normalize-space(.), 'STATE', 'state'), 'state')]/following::select[1]"),
                By.xpath("//label[contains(translate(normalize-space(.), 'STATE', 'state'), 'state')]/following::input[@role='combobox'][1]"),
                By.xpath("//label[contains(translate(normalize-space(.), 'STATE', 'state'), 'state')]/following::div[contains(@role,'combobox') or contains(@class,'state')][1]"),
                By.xpath("//input[@role='combobox' and (contains(translate(@aria-label, 'STATE', 'state'), 'state') or contains(translate(@placeholder, 'STATE', 'state'), 'state'))]"),
                By.xpath("//select[contains(translate(@id, 'STATE', 'state'), 'state') or contains(translate(@name, 'STATE', 'state'), 'state') or contains(translate(@aria-label, 'STATE', 'state'), 'state')]")));

        List<By> locators = map.get(f);

        if (locators != null) {
            return locators;
        }

        return Arrays.asList(
                By.name(fieldKey),
                By.xpath("//label[contains(translate(., '" + fieldKey.toUpperCase() + "', '" + fieldKey.toLowerCase()
                        + "'), '" + fieldKey.toLowerCase() + "')]/following::input[1]"),
                By.xpath("//input[contains(translate(@placeholder, '" + fieldKey.toUpperCase() + "', '"
                        + fieldKey.toLowerCase() + "'), '" + fieldKey.toLowerCase() + "')]"));
    }

    public WebElement findFieldElement(String fieldKey) {
        for (By locator : getFieldLocators(fieldKey)) {
            try {
                WebElement element = driver.findElement(locator);
                if (element.isDisplayed()) {
                    return element;
                }
            } catch (RuntimeException err) {
                // try next locator
            }
        }

        By labelLocator;
        if ("country".equals(fieldKey)) {
            labelLocator = By.xpath("//label[contains(translate(normalize-space(.), 'COUNTRY', 'country'), 'country')]");
        } else if ("state".equals(fieldKey)) {
            labelLocator = By.xpath("//label[contains(translate(normalize-space(.), 'STATE', 'state'), 'state')]");
        } else {
            String labelText = fieldKey.replaceAll("([A-Z])", " $1").toLowerCase();
            labelLocator = By.xpath("//label[contains(translate(normalize-space(.), "
                    + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + labelText + "')]");
        }

        try {
            for (WebElement label : driver.findElements(labelLocator)) {
                String forAttr = label.getAttribute("for");
                if (forAttr != null && !forAttr.isEmpty()) {
                    try {
                        WebElement target = driver.findElement(By.id(forAttr));
                        if (target.isDisplayed()) {
                            return target;
                        }
                    } catch (RuntimeException ignored) {
                        // keep looking
                    }
                }

                try {
                    WebElement sibling = label.findElement(By.xpath(
                            "following::select[1] | following::input[1] | following::div[contains(@role, \"combobox\")][1]"));
                    if (sibling.isDisplayed()) {
                        return sibling;
                    }
                } catch (RuntimeException ignored) {
                    // keep looking
                }
            }
        } catch (RuntimeException err) {
            // continue to final fallback
        }

        throw new IllegalStateException("Field '" + fieldKey + "' not found with fallback locators");
    }

    public boolean selectComboboxValue(WebElement inputElement, String value) {
        String normalizedValue = value.trim();
        Helpers.scrollToElement(driver, inputElement);
        inputElement.click();

        try {
            inputElement.clear();
        } catch (RuntimeException ignored) {
            // some custom elements may not support clear
        }
        inputElement.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        inputElement.sendKeys(normalizedValue);

        String ariaControls = inputElement.getAttribute("aria-controls");
        String searchText = normalizedValue.toLowerCase();

        if (ariaControls != null && !ariaControls.isEmpty()) {
            try {
                WebElement listBox = Helpers.wait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.visibilityOfElementLocated(By.id(ariaControls)));

                if (trySelectFromListbox(listBox, searchText)) {
                    return true;
                }
            } catch (RuntimeException err) {
                // continue to global listbox search
            }
        }

        try {
            for (WebElement box : driver.findElements(
                    By.xpath("//*[@role='listbox' or @role='presentation' or @role='menu']"))) {
                if (box.isDisplayed() && trySelectFromListbox(box, searchText)) {
                    return true;
                }
            }
        } catch (RuntimeException err) {
            // no listbox found
        }

        try {
            inputElement.sendKeys(Keys.ARROW_DOWN);
            inputElement.sendKeys(Keys.ENTER);

            String selected = inputElement.getAttribute("value");
            if (selected != null && selected.toLowerCase().contains(searchText)) {
                return true;
            }
        } catch (RuntimeException err) {
            // ignore
        }

        return false;
    }

    private boolean trySelectFromListbox(WebElement listElement, String searchText) {
        try {
            for (WebElement option : listElement.findElements(By.xpath(".//*[@role=\"option\"] | .//li"))) {
                try {
                    String text = option.getText().trim().toLowerCase();
                    if (text.isEmpty()) {
                        continue;
                    }
                    if (text.equals(searchText) || text.contains(searchText)) {
                        Helpers.scrollToElement(driver, option);
                        option.click();
                        return true;
                    }
                } catch (RuntimeException innerErr) {
                    // ignore option read errors
                }
            }
            return false;
        } catch (RuntimeException innerErr) {
            return false;
        }
    }

    public void selectDropdownValue(String fieldKey, String value) {
        WebElement dropdown = null;

        for (By locator : getFieldLocators(fieldKey)) {
            try {
                WebElement element = driver.findElement(locator);
                if (element.isDisplayed()) {
                    dropdown = element;
                    break;
                }
            } catch (RuntimeException err) {
                // try next locator
            }
        }

        if (dropdown == null) {
            throw new IllegalStateException("Dropdown '" + fieldKey + "' not found");
        }

        String tagName = dropdown.getTagName();
        String normalized = value.trim().toLowerCase();

        if ("select".equalsIgnoreCase(tagName)) {
            By exactOptionLocator = By.xpath(".//option[translate(normalize-space(text()), "
                    + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = '" + normalized + "']");
            try {
                dropdown.findElement(exactOptionLocator).click();
                return;
            } catch (RuntimeException err) {
                // fallback contains
            }

            By partialOptionLocator = By.xpath(".//option[contains(translate(normalize-space(text()), "
                    + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + normalized + "')]");
            dropdown.findElement(partialOptionLocator).click();
            return;
        }

        dropdown.click();

        if ("input".equalsIgnoreCase(tagName) && selectComboboxValue(dropdown, value)) {
            return;
        }

        try {
            dropdown.clear();
        } catch (RuntimeException err) {
            // some custom elements may not support clear
        }

        try {
            dropdown.sendKeys(value);
        } catch (RuntimeException err) {
            // ignore
        }

        List<By> dropdownOptionLocators = Arrays.asList(
                By.xpath("//li[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', "
                        + "'abcdefghijklmnopqrstuvwxyz'), '" + normalized + "')]"),
                By.xpath("//div[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', "
                        + "'abcdefghijklmnopqrstuvwxyz'), '" + normalized + "') and (contains(@role, 'option') "
                        + "or contains(@class, 'option') or contains(@id, 'option'))]"),
                By.xpath("//span[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', "
                        + "'abcdefghijklmnopqrstuvwxyz'), '" + normalized + "') and contains(@class, 'option')]"),
                By.xpath("//div[@role='option' and contains(translate(normalize-space(.), "
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + normalized + "')]"),
                By.xpath("//div[contains(@role, 'listbox')]//div[contains(translate(normalize-space(.), "
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + normalized + "')]"));

        for (By locator : dropdownOptionLocators) {
            try {
                WebElement option = Helpers.wait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.visibilityOfElementLocated(locator));
                Helpers.scrollToElement(driver, option);
                option.click();
                return;
            } catch (RuntimeException err) {
                // try next
            }
        }

        try {
            dropdown.sendKeys(Keys.ARROW_DOWN, Keys.ENTER);
            String selectedValue = dropdown.getAttribute("value");
            if (selectedValue != null && selectedValue.toLowerCase().contains(normalized)) {
                return;
            }
        } catch (RuntimeException err) {
            // ignore
        }

        throw new IllegalStateException("Dropdown option '" + value + "' not found for '" + fieldKey + "'");
    }

    public void fillCustomerDetails(CustomerDetails details) {
        List<String[]> fields = new ArrayList<>();
        fields.add(new String[] { "email", details.email });
        fields.add(new String[] { "firstName", details.firstName });
        fields.add(new String[] { "lastName", details.lastName });
        fields.add(new String[] { "phone", details.phone });
        fields.add(new String[] { "country", details.country });
        fields.add(new String[] { "address1", details.address1 });
        fields.add(new String[] { "city", details.city });
        fields.add(new String[] { "state", details.region });
        fields.add(new String[] { "zip", details.zip });

        for (String[] field : fields) {
            String key = field[0];
            String value = field[1];

            if (value == null || value.isEmpty()) {
                continue;
            }

            try {
                if ("country".equals(key) || "state".equals(key)) {
                    selectDropdownValue(key, value);
                    continue;
                }

                WebElement element = findFieldElement(key);
                if (element.isDisplayed()) {
                    Helpers.scrollToElement(driver, element);
                    try {
                        element.clear();
                    } catch (RuntimeException ignored) {
                        // some inputs reject clear()
                    }
                    element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
                    element.sendKeys(value);

                    if ("address1".equals(key)) {
                        // Typing the address opens a Google Places autocomplete
                        // suggestion list; dismiss it immediately so it doesn't
                        // intercept the next field's click or get selected by
                        // accident, silently overwriting city/state/zip.
                        element.sendKeys(Keys.ESCAPE);
                    }
                }
            } catch (RuntimeException err) {
                System.out.println("Unable to fill field '" + key + "': " + err.getMessage());
            }
        }
    }

    /**
     * Advances through every remaining checkout accordion section (customer &
     * delivery details, delivery method, payment) and places the order,
     * clicking each section's own "Continue" button in turn until the final
     * "Place Order & Pay" button is reached.
     */
    public void placeOrder() {
        for (int attempt = 0; attempt < 5; attempt++) {
            // Wait for either this section's own "Continue" button or the
            // final "Place Order & Pay" button to render before deciding what
            // to do - each accordion section can take a moment to appear.
            Helpers.wait(driver, Duration.ofSeconds(15)).until(d ->
                    isVisible(getFinalPlaceOrderButton()) || isVisible(getContinueOnlyButton()));

            if (isVisible(getFinalPlaceOrderButton())) {
                WebElement finalButton = waitForClickable(getFinalPlaceOrderButton(), Duration.ofSeconds(10));
                Helpers.scrollToElement(driver, finalButton);
                finalButton.click();
                return;
            }

            WebElement continueButton = waitForClickable(getContinueOnlyButton(), Duration.ofSeconds(10));
            Helpers.scrollToElement(driver, continueButton);
            continueButton.click();
        }
    }

    public void waitForCheckoutPageLoad() {
        waitFor(getCheckoutTitle(), Duration.ofSeconds(10));
    }
}
