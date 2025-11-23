package org.example.dao;

import org.example.model.CustomerAccount;
import org.example.model.PaymentRecord;
import org.example.model.PurchaseTransaction;
import org.example.util.TestDatabaseHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentRecordDAOTest {
    private PaymentRecordDAO dao;
    private PurchaseTransactionDAO transactionDAO;
    private CustomerAccountDAO accountDAO;

    @BeforeEach
    void setUp() throws Exception {
        TestDatabaseHelper.setupTestDatabase();
        dao = new PaymentRecordDAO();
        transactionDAO = new PurchaseTransactionDAO();
        accountDAO = new CustomerAccountDAO();
    }

    @AfterEach
    void tearDown() throws Exception {
        TestDatabaseHelper.cleanupTestDatabase();
        TestDatabaseHelper.restoreOriginalDatabase();
    }

    private int createTestOrder() {
        String uniqueUsername = "testuser_" + System.nanoTime();
        CustomerAccount account = new CustomerAccount(uniqueUsername, "pass", "CUSTOMER");
        accountDAO.insertRecord(account);
        CustomerAccount found = accountDAO.authenticateUser(uniqueUsername, "pass");
        
        PurchaseTransaction transaction = new PurchaseTransaction(found.getAccountId(), "CREATED", 100.00);
        return transactionDAO.insertTransaction(transaction);
    }

    @Test
    void testInsertRecord_Success() {
        int orderId = createTestOrder();
        PaymentRecord payment = new PaymentRecord(orderId, "ONLINE", "SUCCESS", 100.00);
        
        boolean result = dao.insertRecord(payment);
        assertTrue(result);
    }

    @Test
    void testInsertRecord_AllPaymentTypes() {
        int orderId1 = createTestOrder();
        int orderId2 = createTestOrder();
        int orderId3 = createTestOrder();
        
        assertTrue(dao.insertRecord(new PaymentRecord(orderId1, "ONLINE", "SUCCESS", 50.00)));
        assertTrue(dao.insertRecord(new PaymentRecord(orderId2, "CARD", "SUCCESS", 75.00)));
        assertTrue(dao.insertRecord(new PaymentRecord(orderId3, "COD", "SUCCESS", 100.00)));
    }

    @Test
    void testInsertRecord_AllPaymentStatuses() {
        int orderId1 = createTestOrder();
        int orderId2 = createTestOrder();
        int orderId3 = createTestOrder();
        
        assertTrue(dao.insertRecord(new PaymentRecord(orderId1, "ONLINE", "SUCCESS", 50.00)));
        assertTrue(dao.insertRecord(new PaymentRecord(orderId2, "CARD", "FAILED", 75.00)));
        assertTrue(dao.insertRecord(new PaymentRecord(orderId3, "COD", "REFUNDED", 100.00)));
    }

    @Test
    void testLocateByTransactionId_Success() {
        int orderId = createTestOrder();
        PaymentRecord payment = new PaymentRecord(orderId, "CARD", "SUCCESS", 150.75);
        dao.insertRecord(payment);
        
        PaymentRecord found = dao.locateByTransactionId(orderId);
        assertNotNull(found);
        assertEquals(orderId, found.getTransactionId());
        assertEquals("CARD", found.getPaymentType());
        assertEquals("SUCCESS", found.getPaymentStatus());
        assertEquals(150.75, found.getPaymentAmount());
        assertNotNull(found.getPaymentTimestamp());
    }

    @Test
    void testLocateByTransactionId_NonExistent() {
        PaymentRecord found = dao.locateByTransactionId(99999);
        assertNull(found);
    }

    @Test
    void testModifyPaymentStatus_Success() {
        int orderId = createTestOrder();
        PaymentRecord payment = new PaymentRecord(orderId, "ONLINE", "SUCCESS", 200.00);
        dao.insertRecord(payment);
        
        boolean result = dao.modifyPaymentStatus(orderId, "REFUNDED");
        assertTrue(result);
        
        PaymentRecord updated = dao.locateByTransactionId(orderId);
        assertEquals("REFUNDED", updated.getPaymentStatus());
    }

    @Test
    void testModifyPaymentStatus_NonExistent() {
        boolean result = dao.modifyPaymentStatus(99999, "REFUNDED");
        assertFalse(result);
    }

    @Test
    void testModifyPaymentStatus_MultipleStatuses() {
        int orderId = createTestOrder();
        PaymentRecord payment = new PaymentRecord(orderId, "ONLINE", "SUCCESS", 100.00);
        dao.insertRecord(payment);
        
        dao.modifyPaymentStatus(orderId, "FAILED");
        PaymentRecord updated1 = dao.locateByTransactionId(orderId);
        assertEquals("FAILED", updated1.getPaymentStatus());
        
        dao.modifyPaymentStatus(orderId, "REFUNDED");
        PaymentRecord updated2 = dao.locateByTransactionId(orderId);
        assertEquals("REFUNDED", updated2.getPaymentStatus());
    }
}

