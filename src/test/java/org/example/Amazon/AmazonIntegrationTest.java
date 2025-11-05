package org.example.Amazon;

import org.example.Amazon.Cost.DeliveryPrice;
import org.example.Amazon.Cost.ExtraCostForElectronics;
import org.example.Amazon.Cost.ItemType;
import org.example.Amazon.Cost.PriceRule;
import org.example.Amazon.Cost.RegularCost;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// This is an integration test, so we use real implementations, not mocks.
class AmazonIntegrationTest {

    private org.example.Amazon.Database database;
    private ShoppingCart shoppingCart;

    @BeforeEach
    void setUp() {
        // Per assignment, reset the DB before each test
        // This creates a fresh in-memory database for each test
        database = new org.example.Amazon.Database();
        database.resetDatabase();
        // We use the real ShoppingCartAdaptor which talks to the real Database
        shoppingCart = new ShoppingCartAdaptor(database);
    }

    @AfterEach
    void tearDown() {
        // Close the database connection after each test
        database.close();
    }

    @Test
    @DisplayName("specification-based")
    void test_integration_EmptyCart() {
        // Specification: An empty cart should have a total cost of 0.
        List<PriceRule> rules = List.of(new RegularCost(), new DeliveryPrice());
        Amazon amazon = new Amazon(shoppingCart, rules);

        double finalPrice = amazon.calculate();

        // RegularCost is 0, DeliveryPrice is 0
        assertThat(finalPrice).isZero();
    }

    @Test
    @DisplayName("specification-based")
    void test_integration_OneItem_Other() {
        // Specification: A single non-electronic item.
        List<PriceRule> rules = List.of(new RegularCost(), new DeliveryPrice(), new ExtraCostForElectronics());
        Amazon amazon = new Amazon(shoppingCart, rules);

        amazon.addToCart(new Item(ItemType.OTHER, "Book", 1, 20.0));

        double finalPrice = amazon.calculate();

        // RegularCost = 20.0
        // DeliveryPrice (1 item) = 5.0
        // ExtraCost (no electronics) = 0.0
        // Total = 25.0
        assertThat(finalPrice).isEqualTo(25.0);
    }

    @Test
    @DisplayName("specification-based")
    void test_integration_TwoItems_WithElectronic() {
        // Specification: A cart with an electronic item should trigger the extra fee.
        List<PriceRule> rules = List.of(new RegularCost(), new DeliveryPrice(), new ExtraCostForElectronics());
        Amazon amazon = new Amazon(shoppingCart, rules);

        amazon.addToCart(new Item(ItemType.OTHER, "Book", 1, 20.0));
        amazon.addToCart(new Item(ItemType.ELECTRONIC, "Laptop", 1, 1000.0));

        double finalPrice = amazon.calculate();

        // RegularCost = 20.0 + 1000.0 = 1020.0
        // DeliveryPrice (2 items) = 5.0
        // ExtraCost (has electronic) = 7.50
        // Total = 1032.50
        assertThat(finalPrice).isEqualTo(1032.50);
    }

    @Test
    // FIX: Combined DisplayName. You cannot have two @DisplayName annotations.
    @DisplayName("specification-based / structural-based")
    void test_integration_MidRangeDelivery() {
        // Specification: A cart with 4-10 items should trigger the mid-range delivery fee.
        // Structural: This tests the (totalItems >= 4 && totalItems <= 10) branch in DeliveryPrice.
        List<PriceRule> rules = List.of(new RegularCost(), new DeliveryPrice());
        Amazon amazon = new Amazon(shoppingCart, rules);

        amazon.addToCart(new Item(ItemType.OTHER, "Pencil", 5, 1.0)); // 5.0
        amazon.addToCart(new Item(ItemType.OTHER, "Book", 1, 15.0));   // 15.0

        // Total 2 items in cart (shoppingCart.getItems().size() == 2)
        // Note: The DeliveryPrice logic is based on number of *item lines*, not quantity.
        // Let's add 4 distinct items
        shoppingCart.add(new Item(ItemType.OTHER, "Eraser", 1, 2.0)); // 2.0

        // FIX: This is the line that had the typo
        shoppingCart.add(new Item(ItemType.OTHER, "Stapler", 1, 10.0)); // 10.0

        // Now we have 4 items in the cart
        double finalPrice = amazon.calculate();

        // RegularCost = 5.0 + 15.0 + 2.0 + 10.0 = 32.0
        // DeliveryPrice (4 items) = 12.5
        // Total = 44.5
        assertThat(finalPrice).isEqualTo(44.5);
    }

    @Test
    @DisplayName("structural-based")
    void test_integration_MaxRangeDelivery() {
        // Structural: This tests the final 'return 20.0' branch (> 10 items) in DeliveryPrice.
        List<PriceRule> rules = List.of(new RegularCost(), new DeliveryPrice());
        Amazon amazon = new Amazon(shoppingCart, rules);

        // Add 11 items
        for(int i = 0; i < 11; i++) {
            amazon.addToCart(new Item(ItemType.OTHER, "Item " + i, 1, 1.0));
        }

        double finalPrice = amazon.calculate();

        // RegularCost = 11 * 1.0 = 11.0
        // DeliveryPrice (11 items) = 20.0
        // Total = 31.0
        assertThat(finalPrice).isEqualTo(31.0);
    }
}