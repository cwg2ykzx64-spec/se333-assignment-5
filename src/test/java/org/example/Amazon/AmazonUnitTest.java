package org.example.Amazon;

import org.example.Amazon.Cost.ItemType;
import org.example.Amazon.Cost.PriceRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Use MockitoExtension to enable mock creation
@ExtendWith(MockitoExtension.class)
class AmazonUnitTest {

    // Mocks for external dependencies, as required by Part 3 for unit tests
    @Mock
    private ShoppingCart cart;
    @Mock
    private PriceRule rule1;
    @Mock
    private PriceRule rule2;

    @Test
    @DisplayName("specification-based / structural-based")
    void test_calculate_NoRules() {
        // Specification: If no rules are provided, the total price should be 0.
        // Structural: This covers the 'for' loop executing 0 times.

        Amazon amazon = new Amazon(cart, List.of()); // Empty list of rules

        double finalPrice = amazon.calculate();
        assertThat(finalPrice).isZero();
    }

    @Test
    @DisplayName("specification-based / structural-based")
    void test_calculate_OneRule() {
        // Specification: With one rule, the price is just the result of that rule.
        // Structural: This covers the 'for' loop executing 1 time.
        List<Item> items = List.of(new Item(ItemType.OTHER, "Book", 1, 10.0));
        when(cart.getItems()).thenReturn(items);
        when(rule1.priceToAggregate(items)).thenReturn(10.0);

        Amazon amazon = new Amazon(cart, List.of(rule1));

        double finalPrice = amazon.calculate();
        assertThat(finalPrice).isEqualTo(10.0);
    }

    @Test
    @DisplayName("specification-based / structural-based")
    void test_calculate_MultipleRules() {
        // Specification: With multiple rules, the price is the sum of all rule results.
        // Structural: This covers the 'for' loop executing N (>1) times.
        List<Item> items = List.of(new Item(ItemType.OTHER, "Book", 1, 10.0));
        when(cart.getItems()).thenReturn(items);

        // Define mock behavior
        when(rule1.priceToAggregate(items)).thenReturn(10.0); // e.g., regular cost
        when(rule2.priceToAggregate(items)).thenReturn(5.0);  // e.g., delivery cost

        Amazon amazon = new Amazon(cart, List.of(rule1, rule2));

        double finalPrice = amazon.calculate();

        // Expected: 10.0 + 5.0 = 15.0
        assertThat(finalPrice).isEqualTo(15.0);
    }

    @Test
    @DisplayName("specification-based")
    void test_calculate_NoItems() {
        // Specification: If the cart is empty, the rules should calculate based on an empty list.
        List<Item> emptyList = List.of();
        when(cart.getItems()).thenReturn(emptyList);
        when(rule1.priceToAggregate(emptyList)).thenReturn(0.0);

        Amazon amazon = new Amazon(cart, List.of(rule1));

        double finalPrice = amazon.calculate();
        assertThat(finalPrice).isZero();
    }

    @Test
    @DisplayName("specification-based")
    void test_addToCart() {
        // Specification: Calling addToCart on Amazon should delegate the call
        // to the ShoppingCart implementation.
        Amazon amazon = new Amazon(cart, List.of());
        Item itemToAdd = new Item(ItemType.OTHER, "Book", 1, 10.0);

        amazon.addToCart(itemToAdd);

        // Verify that cart.add() was called exactly once with the correct item
        verify(cart, times(1)).add(itemToAdd);
    }
}