Feature: Deals & Offers filtering and load more flow
  As a customer
  I want to validate the Deals & Offers product listing behavior
  So that I can verify filtering, pagination, and price-range behavior

  @deals-offers-flow
  Scenario: Validate Deals offers product filtering and load more flow
    Given I navigate to the Shopbricks website
    When I click on the Deals and Offers section
    And I click the All Products link under Browse by
    Then I should see at least 10 products on the page
    When I click Load More three times
    And I set the price filter to a maximum of 300
    Then I should see only products priced at 300 or less
    When I set the price filter to a maximum of 100
    Then I should see the price filter span text on the page
