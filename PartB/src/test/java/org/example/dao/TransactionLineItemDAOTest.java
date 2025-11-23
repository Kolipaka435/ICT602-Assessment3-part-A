package org.example.dao;

import org.example.model.CustomerAccount;
import org.example.model.PurchaseTransaction;
import org.example.model.TransactionLineItem;
import org.example.util.TestDatabaseHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TransactionLineItemDAOTest {
    private TransactionLineItemDAO dao;
    private PurchaseTransactionDAO transactionDAO;
    private CustomerAccountDAO accountDAO;
    private InventoryItemDAO inventoryDAO;

    @BeforeEach
    void setUp() throws Exception {
        TestDatabaseHelper.setupTestDatabase();
        dao = new TransactionLineItemDAO();
        transactionDAO = new PurchaseTransactionDAO();
        accountDAO = new CustomerAccountDAO();
        inventoryDAO = new InventoryItemDAO();
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

    private int createTestProduct(String name, double price) {
        org.example.model.InventoryItem item = new org.example.model.InventoryItem(name, "Description", price, 10);
        return inventoryDAO.insertRecord(item);
    }

    @Test
    void testInsertRecord_Success() {
        int orderId = createTestOrder();
        int productId = createTestProduct("Product 1", 29.99);
        TransactionLineItem lineItem = new TransactionLineItem(orderId, productId, 5, 29.99);
        
        boolean result = dao.insertRecord(lineItem);
        assertTrue(result);
    }

    @Test
    void testInsertRecord_MultipleItems() {
        int orderId = createTestOrder();
        int productId1 = createTestProduct("Product 1", 10.00);
        int productId2 = createTestProduct("Product 2", 20.00);
        int productId3 = createTestProduct("Product 3", 30.00);
        
        assertTrue(dao.insertRecord(new TransactionLineItem(orderId, productId1, 2, 10.00)));
        assertTrue(dao.insertRecord(new TransactionLineItem(orderId, productId2, 3, 20.00)));
        assertTrue(dao.insertRecord(new TransactionLineItem(orderId, productId3, 1, 30.00)));
    }

    @Test
    void testLocateByTransactionId_Success() {
        int orderId = createTestOrder();
        int productId1 = createTestProduct("Product 1", 15.50);
        int productId2 = createTestProduct("Product 2", 25.75);
        TransactionLineItem item1 = new TransactionLineItem(orderId, productId1, 2, 15.50);
        TransactionLineItem item2 = new TransactionLineItem(orderId, productId2, 3, 25.75);
        
        dao.insertRecord(item1);
        dao.insertRecord(item2);
        
        List<TransactionLineItem> items = dao.locateByTransactionId(orderId);
        assertEquals(2, items.size());
        
        TransactionLineItem found1 = items.get(0);
        assertEquals(orderId, found1.getTransactionId());
        assertTrue(found1.getInventoryItemId() == productId1 || found1.getInventoryItemId() == productId2);
        
        TransactionLineItem found2 = items.get(1);
        assertEquals(orderId, found2.getTransactionId());
        assertTrue(found2.getInventoryItemId() == productId1 || found2.getInventoryItemId() == productId2);
    }

    @Test
    void testLocateByTransactionId_Empty() {
        int orderId = createTestOrder();
        List<TransactionLineItem> items = dao.locateByTransactionId(orderId);
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    void testLocateByTransactionId_NonExistent() {
        List<TransactionLineItem> items = dao.locateByTransactionId(99999);
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    void testLocateByTransactionId_MultipleOrders() {
        int orderId1 = createTestOrder();
        int orderId2 = createTestOrder();
        int productId1 = createTestProduct("Product 1", 10.00);
        int productId2 = createTestProduct("Product 2", 20.00);
        int productId3 = createTestProduct("Product 3", 30.00);
        
        dao.insertRecord(new TransactionLineItem(orderId1, productId1, 2, 10.00));
        dao.insertRecord(new TransactionLineItem(orderId1, productId2, 1, 20.00));
        dao.insertRecord(new TransactionLineItem(orderId2, productId3, 5, 30.00));
        
        List<TransactionLineItem> items1 = dao.locateByTransactionId(orderId1);
        assertEquals(2, items1.size());
        
        List<TransactionLineItem> items2 = dao.locateByTransactionId(orderId2);
        assertEquals(1, items2.size());
    }

    @Test
    void testInsertRecord_WithZeroQuantity() {
        int orderId = createTestOrder();
        int productId = createTestProduct("Product 1", 29.99);
        TransactionLineItem lineItem = new TransactionLineItem(orderId, productId, 0, 29.99);
        boolean result = dao.insertRecord(lineItem);
        assertTrue(result);
    }

    @Test
    void testInsertRecord_WithZeroPrice() {
        int orderId = createTestOrder();
        int productId = createTestProduct("Product 1", 0.0);
        TransactionLineItem lineItem = new TransactionLineItem(orderId, productId, 5, 0.0);
        boolean result = dao.insertRecord(lineItem);
        assertTrue(result);
    }
}

