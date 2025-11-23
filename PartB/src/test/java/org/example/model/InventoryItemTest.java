package org.example.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InventoryItemTest {
    private InventoryItem item;

    @BeforeEach
    void setUp() {
        item = new InventoryItem();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(item);
        assertEquals(0, item.getItemId());
        assertNull(item.getItemName());
        assertNull(item.getItemDescription());
        assertEquals(0.0, item.getItemPrice());
        assertEquals(0, item.getItemStock());
    }

    @Test
    void testConstructorWithNameDescriptionPriceStock() {
        InventoryItem newItem = new InventoryItem("Shirt", "Blue cotton shirt", 29.99, 50);
        assertEquals("Shirt", newItem.getItemName());
        assertEquals("Blue cotton shirt", newItem.getItemDescription());
        assertEquals(29.99, newItem.getItemPrice());
        assertEquals(50, newItem.getItemStock());
        assertEquals(0, newItem.getItemId());
    }

    @Test
    void testFullConstructor() {
        InventoryItem newItem = new InventoryItem(1, "Pants", "Black jeans", 49.99, 30);
        assertEquals(1, newItem.getItemId());
        assertEquals("Pants", newItem.getItemName());
        assertEquals("Black jeans", newItem.getItemDescription());
        assertEquals(49.99, newItem.getItemPrice());
        assertEquals(30, newItem.getItemStock());
    }

    @Test
    void testSettersAndGetters() {
        item.setItemId(10);
        item.setItemName("Shoes");
        item.setItemDescription("Running shoes");
        item.setItemPrice(79.99);
        item.setItemStock(25);

        assertEquals(10, item.getItemId());
        assertEquals("Shoes", item.getItemName());
        assertEquals("Running shoes", item.getItemDescription());
        assertEquals(79.99, item.getItemPrice());
        assertEquals(25, item.getItemStock());
    }

    @Test
    void testSetPriceWithZero() {
        item.setItemPrice(0.0);
        assertEquals(0.0, item.getItemPrice());
    }

    @Test
    void testSetPriceWithNegative() {
        item.setItemPrice(-10.0);
        assertEquals(-10.0, item.getItemPrice());
    }

    @Test
    void testSetStockWithZero() {
        item.setItemStock(0);
        assertEquals(0, item.getItemStock());
    }

    @Test
    void testSetStockWithNegative() {
        item.setItemStock(-5);
        assertEquals(-5, item.getItemStock());
    }
}

