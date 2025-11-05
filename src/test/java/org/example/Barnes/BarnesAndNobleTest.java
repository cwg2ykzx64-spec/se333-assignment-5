package org.example.Barnes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Use MockitoExtension to enable mock injection
@ExtendWith(MockitoExtension.class)
class BarnesAndNobleTest {

    // Create a mock for the BookDatabase interface
    @Mock
    private BookDatabase bookDatabase;

    // Create a mock for the BuyBookProcess interface
    @Mock
    private BuyBookProcess process;

    // Inject the mocks (database and process) into a new instance of BarnesAndNoble
    @InjectMocks
    private BarnesAndNoble barnesAndNoble;

    private Book book1_inStock;
    private Book book2_partialStock;

    @BeforeEach
    void setUp() {
        // Define common book objects for tests
        book1_inStock = new Book("ISBN1", 10, 5); // 5 in stock
        book2_partialStock = new Book("ISBN2", 20, 3); // 3 in stock
    }

    @Test
    @DisplayName("specification-based")
    void test_getPrice_NullCart() {
        // Specification: If the order map is null, the method should return null.
        PurchaseSummary summary = barnesAndNoble.getPriceForCart(null);
        assertThat(summary).isNull();
    }

    @Test
    @DisplayName("specification-based")
    void test_getPrice_EmptyCart() {
        // Specification: An empty order should result in a total price of 0
        // and no unavailable items.
        Map<String, Integer> order = Collections.emptyMap();
        PurchaseSummary summary = barnesAndNoble.getPriceForCart(order);

        assertThat(summary.getTotalPrice()).isZero();
        assertThat(summary.getUnavailable()).isEmpty();
        // Verify that no books were processed
        verify(process, never()).buyBook(any(), any(Integer.class));
    }

    @Test
    @DisplayName("specification-based")
    void test_getPrice_SingleBook_InStock() {
        // Specification: Purchasing a book that is fully in stock.
        Map<String, Integer> order = Map.of("ISBN1", 2); // Order 2

        // Mock the database response
        when(bookDatabase.findByISBN("ISBN1")).thenReturn(book1_inStock);

        PurchaseSummary summary = barnesAndNoble.getPriceForCart(order);

        // Expected price: 2 * 10 = 20
        assertThat(summary.getTotalPrice()).isEqualTo(20);
        assertThat(summary.getUnavailable()).isEmpty();
        // Verify the purchase process was called with the correct quantity
        verify(process).buyBook(book1_inStock, 2);
    }

    @Test
    @DisplayName("specification-based / structural-based")
    void test_getPrice_SingleBook_PartialStock() {
        // Specification: Purchasing a book with insufficient stock.
        // Structural: This test covers the 'if (book.getQuantity() < quantity)' branch.
        Map<String, Integer> order = Map.of("ISBN2", 5); // Order 5, but only 3 in stock

        // Mock the database response
        when(bookDatabase.findByISBN("ISBN2")).thenReturn(book2_partialStock);

        PurchaseSummary summary = barnesAndNoble.getPriceForCart(order);

        // Expected price: 3 * 20 = 60 (only 3 were available)
        assertThat(summary.getTotalPrice()).isEqualTo(60);
        // Expected unavailable: 5 (ordered) - 3 (stock) = 2
        assertThat(summary.getUnavailable()).hasSize(1);
        assertThat(summary.getUnavailable()).containsEntry(book2_partialStock, 2);
        // Verify the purchase process was called with the available quantity (3)
        verify(process).buyBook(book2_partialStock, 3);
    }

    @Test
    @DisplayName("specification-based")
    void test_getPrice_SingleBook_OutOfStock() {
        // Specification: Purchasing a book that is completely out of stock.
        Map<String, Integer> order = Map.of("ISBN1", 2); // Order 2
        Book outOfStockBook = new Book("ISBN1", 10, 0); // 0 in stock

        when(bookDatabase.findByISBN("ISBN1")).thenReturn(outOfStockBook);

        PurchaseSummary summary = barnesAndNoble.getPriceForCart(order);

        // Expected price: 0 * 10 = 0
        assertThat(summary.getTotalPrice()).isZero();
        // Expected unavailable: 2 (ordered) - 0 (stock) = 2
        assertThat(summary.getUnavailable()).hasSize(1);
        assertThat(summary.getUnavailable()).containsEntry(outOfStockBook, 2);
        // Verify the purchase process was called with quantity 0
        verify(process).buyBook(outOfStockBook, 0);
    }

    @Test
    @DisplayName("specification-based")
    void test_getPrice_MultipleBooks_MixedStock() {
        // Specification: A complex order with multiple books, one in stock, one partial.
        Map<String, Integer> order = Map.of(
                "ISBN1", 2,  // Order 2 of book1 (Price 10)
                "ISBN2", 5   // Order 5 of book2 (Price 20)
        );

        // Mock database responses
        when(bookDatabase.findByISBN("ISBN1")).thenReturn(book1_inStock); // 5 in stock
        when(bookDatabase.findByISBN("ISBN2")).thenReturn(book2_partialStock); // 3 in stock

        PurchaseSummary summary = barnesAndNoble.getPriceForCart(order);

        // Expected price: (2 * 10) + (3 * 20) = 20 + 60 = 80
        assertThat(summary.getTotalPrice()).isEqualTo(80);
        // Expected unavailable: book2, 5 (ordered) - 3 (stock) = 2
        assertThat(summary.getUnavailable()).hasSize(1);
        assertThat(summary.getUnavailable()).containsEntry(book2_partialStock, 2);
        // Verify process was called for both books with correct quantities
        verify(process).buyBook(book1_inStock, 2);
        verify(process).buyBook(book2_partialStock, 3);
        verify(process, times(2)).buyBook(any(), any(Integer.class));
    }
}