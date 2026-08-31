Feature: Data driven storefront validation
  As a QA engineer
  I want the storefront validated against the expected data held in shared-objects/shopbricks-data.json
  So that page titles, catalogue contents, product details and navigation stay in sync with the published data set

  @data-driven @catalog-data
  Scenario Outline: Validate the <categoryKey> catalogue against the expected data set
    Given I open the "<categoryKey>" category page
    Then the page title should match the expected title for "<categoryKey>"
    And I should see at least the expected number of products for "<categoryKey>"
    And every expected product for "<categoryKey>" should be listed
    And every listed product price should match the expected price for "<categoryKey>"

    Examples:
      | categoryKey         |
      | hardware            |
      | power-tools         |
      | lighting-electrical |
      | deals-offers        |

  @data-driven @product-data
  Scenario Outline: Validate the product details of <slug> against the expected data set
    Given I open the product page for "<slug>"
    Then the product page title should match the expected title for "<slug>"
    And the product name, price and SKU should match the expected data for "<slug>"
    And the Add to Cart button should be available on the product page

    Examples:
      | slug                                          |
      | pipe-wrench-8-in-length                       |
      | steel-grip-claw-hammer                        |
      | black-desk-lamp                               |
      | shawns-20-volt-brushed-cordless-compact-drill |

  @data-driven @navigation-data
  Scenario: Validate the Browse by sidebar and price filter bounds against the expected data set
    Given I open the "all-products" category page
    Then I should see the Browse by heading from the data file
    And the Browse by category list should match the expected categories
    And the price filter bounds should match the expected range for "all-products"
