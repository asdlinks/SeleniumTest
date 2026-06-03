Feature: Shopbricks Checkout Flow
  As a customer
  I want to navigate to deals & offers, select a hardware product, add it to cart, and complete checkout
  So that I can purchase a pipe wrench and receive order confirmation

  Scenario: Complete checkout flow for pipe wrench purchase
    Given I navigate to the Shopbricks website
    When I click on the Deals and Offers section
    And I select the Hardware category
    And I search for and select the Pipe Wrench product
    And I add the product to the cart
    Then I should see the product added to cart notification
    When I proceed to checkout
    And I fill in the customer details:
      | Field              | Value             |
      | Email              | test@example.com  |
      | First name         | John              |
      | Last name          | Doe               |
      | Address            | 123 Main St       |
      | City               | New York          |
      | Country/Region     | United States     |
      | Region/State       | Alaska          |
      | Zip / Postal code  | 10001             |
      | Phone              | 5551234567        |
    And I click the Place Order button
    Then I should see the Thank You message confirming the order
    And I should see the order confirmation details

