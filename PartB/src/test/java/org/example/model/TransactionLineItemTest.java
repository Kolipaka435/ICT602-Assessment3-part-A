package org.example.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TransactionLineItemTest {
    private TransactionLineItem lineItem;

    @BeforeEach
    void setUp() {
        lineItem = new TransactionLineItem();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(lineItem);
        assertEquals(0, lineItem.getLineItemId());
        assertEquals(0, lineItem.getTransactionId());
        assertEquals(0, lineItem.getInventoryItemId());
        assertEquals(0, lineItem.getItemQuantity());
        assertEquals(0.0, lineItem.getItemPriceAtPurchase());
    }

    @Test
    void testConstructorWithTransactionIdItemIdQuantityPrice() {
        TransactionLineItem newItem = new TransactionLineItem(1, 2, 3, 29.99);
        assertEquals(1, newItem.getTransactionId());
        assertEquals(2, newItem.getInventoryItemId());
        assertEquals(3, newItem.getItemQuantity());
        assertEquals(29.99, newItem.getItemPriceAtPurchase());
        assertEquals(0, newItem.getLineItemId());
    }

    @Test
    void testFullConstructor() {
        TransactionLineItem newItem = new TransactionLineItem(10, 5, 3, 2, 49.99);
        assertEquals(10, newItem.getLineItemId());
        assertEquals(5, newItem.getTransactionId());
        assertEquals(3, newItem.getInventoryItemId());
        assertEquals(2, newItem.getItemQuantity());
        assertEquals(49.99, newItem.getItemPriceAtPurchase());
    }

    @Test
    void testSettersAndGetters() {
        lineItem.setLineItemId(15);
        lineItem.setTransactionId(20);
        lineItem.setInventoryItemId(25);
        lineItem.setItemQuantity(4);
        lineItem.setItemPriceAtPurchase(79.99);

        assertEquals(15, lineItem.getLineItemId());
        assertEquals(20, lineItem.getTransactionId());
        assertEquals(25, lineItem.getInventoryItemId());
        assertEquals(4, lineItem.getItemQuantity());
        assertEquals(79.99, lineItem.getItemPriceAtPurchase());
    }

    @Test
    void testSetQuantityWithZero() {
        lineItem.setItemQuantity(0);
        assertEquals(0, lineItem.getItemQuantity());
    }

    @Test
    void testSetQuantityWithNegative() {
        lineItem.setItemQuantity(-5);
        assertEquals(-5, lineItem.getItemQuantity());
    }

    @Test
    void testSetPriceAtPurchaseWithZero() {
        lineItem.setItemPriceAtPurchase(0.0);
        assertEquals(0.0, lineItem.getItemPriceAtPurchase());
    }

    @Test
    void testSetPriceAtPurchaseWithNegative() {
        lineItem.setItemPriceAtPurchase(-10.0);
        assertEquals(-10.0, lineItem.getItemPriceAtPurchase());
    }
}

