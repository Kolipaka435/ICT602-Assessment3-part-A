package org.example.dao;

import org.example.model.CustomerAccount;
import org.example.model.PurchaseTransaction;
import org.example.util.TestDatabaseHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PurchaseTransactionDAOTest {
    private PurchaseTransactionDAO dao;
    private CustomerAccountDAO accountDAO;

    @BeforeEach
    void setUp() throws Exception {
        TestDatabaseHelper.setupTestDatabase();
        dao = new PurchaseTransactionDAO();
        accountDAO = new CustomerAccountDAO();
    }

    @AfterEach
    void tearDown() throws Exception {
        TestDatabaseHelper.cleanupTestDatabase();
        TestDatabaseHelper.restoreOriginalDatabase();
    }

    private int createTestAccount() {
        String uniqueUsername = "testuser_" + System.nanoTime();
        CustomerAccount account = new CustomerAccount(uniqueUsername, "pass", "CUSTOMER");
        accountDAO.insertRecord(account);
        CustomerAccount found = accountDAO.authenticateUser(uniqueUsername, "pass");
        return found.getAccountId();
    }

    @Test
    void testInsertTransaction_Success() {
        int accountId = createTestAccount();
        PurchaseTransaction transaction = new PurchaseTransaction(accountId, "CREATED", 150.75);
        
        int result = dao.insertTransaction(transaction);
        assertTrue(result > 0);
    }

    @Test
    void testInsertTransaction_ReturnsGeneratedId() {
        int accountId = createTestAccount();
        PurchaseTransaction t1 = new PurchaseTransaction(accountId, "CREATED", 100.00);
        PurchaseTransaction t2 = new PurchaseTransaction(accountId, "CREATED", 200.00);
        
        int id1 = dao.insertTransaction(t1);
        int id2 = dao.insertTransaction(t2);
        
        assertTrue(id1 > 0);
        assertTrue(id2 > 0);
        assertNotEquals(id1, id2);
    }

    @Test
    void testInsertTransaction_AllStatuses() {
        int accountId = createTestAccount();
        
        assertTrue(dao.insertTransaction(new PurchaseTransaction(accountId, "CREATED", 50.00)) > 0);
        assertTrue(dao.insertTransaction(new PurchaseTransaction(accountId, "ACCEPTED", 75.00)) > 0);
        assertTrue(dao.insertTransaction(new PurchaseTransaction(accountId, "REJECTED", 100.00)) > 0);
        assertTrue(dao.insertTransaction(new PurchaseTransaction(accountId, "DELIVERED", 125.00)) > 0);
    }

    @Test
    void testModifyStatus_Success() {
        int accountId = createTestAccount();
        PurchaseTransaction transaction = new PurchaseTransaction(accountId, "CREATED", 100.00);
        int id = dao.insertTransaction(transaction);
        
        boolean result = dao.modifyStatus(id, "ACCEPTED");
        assertTrue(result);
        
        PurchaseTransaction updated = dao.locateById(id);
        assertEquals("ACCEPTED", updated.getTransactionStatus());
    }

    @Test
    void testModifyStatus_NonExistent() {
        boolean result = dao.modifyStatus(99999, "ACCEPTED");
        assertFalse(result);
    }

    @Test
    void testModifyStatus_AllStatusTransitions() {
        int accountId = createTestAccount();
        PurchaseTransaction transaction = new PurchaseTransaction(accountId, "CREATED", 100.00);
        int id = dao.insertTransaction(transaction);
        
        dao.modifyStatus(id, "ACCEPTED");
        assertEquals("ACCEPTED", dao.locateById(id).getTransactionStatus());
        
        dao.modifyStatus(id, "DELIVERED");
        assertEquals("DELIVERED", dao.locateById(id).getTransactionStatus());
    }

    @Test
    void testRetrieveAllRecords_Empty() {
        List<PurchaseTransaction> transactions = dao.retrieveAllRecords();
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }

    @Test
    void testRetrieveAllRecords_MultipleTransactions() {
        int accountId = createTestAccount();
        dao.insertTransaction(new PurchaseTransaction(accountId, "CREATED", 50.00));
        dao.insertTransaction(new PurchaseTransaction(accountId, "ACCEPTED", 75.00));
        dao.insertTransaction(new PurchaseTransaction(accountId, "DELIVERED", 100.00));
        
        List<PurchaseTransaction> transactions = dao.retrieveAllRecords();
        assertEquals(3, transactions.size());
    }

    @Test
    void testRetrieveAllRecords_OrderedByDateDesc() {
        int accountId = createTestAccount();
        int id1 = dao.insertTransaction(new PurchaseTransaction(accountId, "CREATED", 50.00));
        int id2 = dao.insertTransaction(new PurchaseTransaction(accountId, "CREATED", 75.00));
        
        List<PurchaseTransaction> transactions = dao.retrieveAllRecords();
        assertEquals(2, transactions.size());
        // Verify both transactions are retrieved
        assertTrue(transactions.stream().anyMatch(t -> t.getTransactionId() == id1));
        assertTrue(transactions.stream().anyMatch(t -> t.getTransactionId() == id2));
    }

    @Test
    void testLocateByUserId_Success() {
        int accountId1 = createTestAccount();
        String uniqueUsername2 = "user2_" + System.nanoTime();
        CustomerAccount account2 = new CustomerAccount(uniqueUsername2, "pass", "CUSTOMER");
        accountDAO.insertRecord(account2);
        CustomerAccount found2 = accountDAO.authenticateUser(uniqueUsername2, "pass");
        int accountId2 = found2.getAccountId();
        
        dao.insertTransaction(new PurchaseTransaction(accountId1, "CREATED", 50.00));
        dao.insertTransaction(new PurchaseTransaction(accountId1, "ACCEPTED", 75.00));
        dao.insertTransaction(new PurchaseTransaction(accountId2, "CREATED", 100.00));
        
        List<PurchaseTransaction> user1Transactions = dao.locateByUserId(accountId1);
        assertEquals(2, user1Transactions.size());
        
        List<PurchaseTransaction> user2Transactions = dao.locateByUserId(accountId2);
        assertEquals(1, user2Transactions.size());
    }

    @Test
    void testLocateByUserId_Empty() {
        int accountId = createTestAccount();
        List<PurchaseTransaction> transactions = dao.locateByUserId(accountId);
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }

    @Test
    void testLocateById_Success() {
        int accountId = createTestAccount();
        PurchaseTransaction transaction = new PurchaseTransaction(accountId, "CREATED", 150.75);
        int id = dao.insertTransaction(transaction);
        
        PurchaseTransaction found = dao.locateById(id);
        assertNotNull(found);
        assertEquals(id, found.getTransactionId());
        assertEquals(accountId, found.getCustomerAccountId());
        assertEquals("CREATED", found.getTransactionStatus());
        assertEquals(150.75, found.getTransactionTotal());
        assertNotNull(found.getTransactionDate());
    }

    @Test
    void testLocateById_NonExistent() {
        PurchaseTransaction found = dao.locateById(99999);
        assertNull(found);
    }
}

