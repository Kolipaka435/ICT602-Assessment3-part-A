package org.example.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class PurchaseTransactionTest {
    private PurchaseTransaction transaction;

    @BeforeEach
    void setUp() {
        transaction = new PurchaseTransaction();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(transaction);
        assertEquals(0, transaction.getTransactionId());
        assertEquals(0, transaction.getCustomerAccountId());
        assertNull(transaction.getTransactionStatus());
        assertNull(transaction.getTransactionDate());
        assertEquals(0.0, transaction.getTransactionTotal());
    }

    @Test
    void testConstructorWithAccountIdStatusTotal() {
        PurchaseTransaction newTransaction = new PurchaseTransaction(1, "CREATED", 150.75);
        assertEquals(1, newTransaction.getCustomerAccountId());
        assertEquals("CREATED", newTransaction.getTransactionStatus());
        assertEquals(150.75, newTransaction.getTransactionTotal());
        assertNotNull(newTransaction.getTransactionDate());
    }

    @Test
    void testFullConstructor() {
        LocalDateTime date = LocalDateTime.of(2024, 1, 15, 14, 30);
        PurchaseTransaction newTransaction = new PurchaseTransaction(5, 2, "ACCEPTED", date, 200.50);
        assertEquals(5, newTransaction.getTransactionId());
        assertEquals(2, newTransaction.getCustomerAccountId());
        assertEquals("ACCEPTED", newTransaction.getTransactionStatus());
        assertEquals(date, newTransaction.getTransactionDate());
        assertEquals(200.50, newTransaction.getTransactionTotal());
    }

    @Test
    void testSettersAndGetters() {
        LocalDateTime date = LocalDateTime.of(2024, 2, 20, 16, 45);
        
        transaction.setTransactionId(10);
        transaction.setCustomerAccountId(3);
        transaction.setTransactionStatus("DELIVERED");
        transaction.setTransactionDate(date);
        transaction.setTransactionTotal(300.25);

        assertEquals(10, transaction.getTransactionId());
        assertEquals(3, transaction.getCustomerAccountId());
        assertEquals("DELIVERED", transaction.getTransactionStatus());
        assertEquals(date, transaction.getTransactionDate());
        assertEquals(300.25, transaction.getTransactionTotal());
    }

    @Test
    void testTransactionStatuses() {
        transaction.setTransactionStatus("CREATED");
        assertEquals("CREATED", transaction.getTransactionStatus());
        
        transaction.setTransactionStatus("ACCEPTED");
        assertEquals("ACCEPTED", transaction.getTransactionStatus());
        
        transaction.setTransactionStatus("REJECTED");
        assertEquals("REJECTED", transaction.getTransactionStatus());
        
        transaction.setTransactionStatus("DELIVERED");
        assertEquals("DELIVERED", transaction.getTransactionStatus());
    }

    @Test
    void testSetTotalWithZero() {
        transaction.setTransactionTotal(0.0);
        assertEquals(0.0, transaction.getTransactionTotal());
    }

    @Test
    void testSetTotalWithNegative() {
        transaction.setTransactionTotal(-100.0);
        assertEquals(-100.0, transaction.getTransactionTotal());
    }
}

