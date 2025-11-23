package org.example.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class PaymentRecordTest {
    private PaymentRecord paymentRecord;

    @BeforeEach
    void setUp() {
        paymentRecord = new PaymentRecord();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(paymentRecord);
        assertEquals(0, paymentRecord.getPaymentRecordId());
        assertEquals(0, paymentRecord.getTransactionId());
        assertNull(paymentRecord.getPaymentType());
        assertNull(paymentRecord.getPaymentStatus());
        assertEquals(0.0, paymentRecord.getPaymentAmount());
        assertNull(paymentRecord.getPaymentTimestamp());
    }

    @Test
    void testConstructorWithTransactionIdPaymentTypeStatusAmount() {
        PaymentRecord newRecord = new PaymentRecord(1, "ONLINE", "SUCCESS", 100.50);
        assertEquals(1, newRecord.getTransactionId());
        assertEquals("ONLINE", newRecord.getPaymentType());
        assertEquals("SUCCESS", newRecord.getPaymentStatus());
        assertEquals(100.50, newRecord.getPaymentAmount());
        assertNotNull(newRecord.getPaymentTimestamp());
    }

    @Test
    void testFullConstructor() {
        LocalDateTime timestamp = LocalDateTime.now();
        PaymentRecord newRecord = new PaymentRecord(1, 2, "CARD", "SUCCESS", 200.75, timestamp);
        assertEquals(1, newRecord.getPaymentRecordId());
        assertEquals(2, newRecord.getTransactionId());
        assertEquals("CARD", newRecord.getPaymentType());
        assertEquals("SUCCESS", newRecord.getPaymentStatus());
        assertEquals(200.75, newRecord.getPaymentAmount());
        assertEquals(timestamp, newRecord.getPaymentTimestamp());
    }

    @Test
    void testSettersAndGetters() {
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30);
        
        paymentRecord.setPaymentRecordId(5);
        paymentRecord.setTransactionId(10);
        paymentRecord.setPaymentType("COD");
        paymentRecord.setPaymentStatus("REFUNDED");
        paymentRecord.setPaymentAmount(150.25);
        paymentRecord.setPaymentTimestamp(timestamp);

        assertEquals(5, paymentRecord.getPaymentRecordId());
        assertEquals(10, paymentRecord.getTransactionId());
        assertEquals("COD", paymentRecord.getPaymentType());
        assertEquals("REFUNDED", paymentRecord.getPaymentStatus());
        assertEquals(150.25, paymentRecord.getPaymentAmount());
        assertEquals(timestamp, paymentRecord.getPaymentTimestamp());
    }

    @Test
    void testPaymentTypes() {
        paymentRecord.setPaymentType("ONLINE");
        assertEquals("ONLINE", paymentRecord.getPaymentType());
        
        paymentRecord.setPaymentType("CARD");
        assertEquals("CARD", paymentRecord.getPaymentType());
        
        paymentRecord.setPaymentType("COD");
        assertEquals("COD", paymentRecord.getPaymentType());
    }

    @Test
    void testPaymentStatuses() {
        paymentRecord.setPaymentStatus("SUCCESS");
        assertEquals("SUCCESS", paymentRecord.getPaymentStatus());
        
        paymentRecord.setPaymentStatus("FAILED");
        assertEquals("FAILED", paymentRecord.getPaymentStatus());
        
        paymentRecord.setPaymentStatus("REFUNDED");
        assertEquals("REFUNDED", paymentRecord.getPaymentStatus());
    }

    @Test
    void testSetAmountWithZero() {
        paymentRecord.setPaymentAmount(0.0);
        assertEquals(0.0, paymentRecord.getPaymentAmount());
    }

    @Test
    void testSetAmountWithNegative() {
        paymentRecord.setPaymentAmount(-50.0);
        assertEquals(-50.0, paymentRecord.getPaymentAmount());
    }
}

