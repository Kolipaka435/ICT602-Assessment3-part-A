package org.example.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartEntryTest {
    private ShoppingCartEntry cartEntry;
    private InventoryItem item;

    @BeforeEach
    void setUp() {
        item = new InventoryItem(1, "Test Product", "Test Description", 25.50, 10);
        cartEntry = new ShoppingCartEntry(item, 2);
    }

    @Test
    void testConstructor() {
        assertNotNull(cartEntry);
        assertEquals(item, cartEntry.getInventoryItem());
        assertEquals(2, cartEntry.getEntryQuantity());
    }

    @Test
    void testGetInventoryItem() {
        assertEquals(item, cartEntry.getInventoryItem());
        assertEquals(1, cartEntry.getInventoryItem().getItemId());
        assertEquals("Test Product", cartEntry.getInventoryItem().getItemName());
    }

    @Test
    void testSetInventoryItem() {
        InventoryItem newItem = new InventoryItem(2, "New Product", "New Description", 30.00, 5);
        cartEntry.setInventoryItem(newItem);
        assertEquals(newItem, cartEntry.getInventoryItem());
        assertEquals(2, cartEntry.getInventoryItem().getItemId());
    }

    @Test
    void testGetEntryQuantity() {
        assertEquals(2, cartEntry.getEntryQuantity());
    }

    @Test
    void testSetEntryQuantity() {
        cartEntry.setEntryQuantity(5);
        assertEquals(5, cartEntry.getEntryQuantity());
    }

    @Test
    void testSetEntryQuantityToZero() {
        cartEntry.setEntryQuantity(0);
        assertEquals(0, cartEntry.getEntryQuantity());
    }

    @Test
    void testSetEntryQuantityToNegative() {
        cartEntry.setEntryQuantity(-1);
        assertEquals(-1, cartEntry.getEntryQuantity());
    }

    @Test
    void testCalculateSubtotal() {
        double expected = 25.50 * 2; // price * quantity
        assertEquals(expected, cartEntry.calculateSubtotal(), 0.01);
    }

    @Test
    void testCalculateSubtotalWithZeroQuantity() {
        cartEntry.setEntryQuantity(0);
        assertEquals(0.0, cartEntry.calculateSubtotal(), 0.01);
    }

    @Test
    void testCalculateSubtotalWithZeroPrice() {
        item.setItemPrice(0.0);
        cartEntry.setInventoryItem(item);
        assertEquals(0.0, cartEntry.calculateSubtotal(), 0.01);
    }

    @Test
    void testCalculateSubtotalWithLargeQuantity() {
        cartEntry.setEntryQuantity(100);
        double expected = 25.50 * 100;
        assertEquals(expected, cartEntry.calculateSubtotal(), 0.01);
    }

    @Test
    void testCalculateSubtotalWithDecimalPrice() {
        item.setItemPrice(19.99);
        cartEntry.setInventoryItem(item);
        cartEntry.setEntryQuantity(3);
        double expected = 19.99 * 3;
        assertEquals(expected, cartEntry.calculateSubtotal(), 0.01);
    }
}

