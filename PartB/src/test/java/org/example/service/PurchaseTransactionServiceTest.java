package org.example.service;

import org.example.model.*;
import org.example.util.TestDatabaseHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PurchaseTransactionServiceTest {
    private PurchaseTransactionService service;
    private InventoryItemService inventoryService;
    private CustomerAccountService accountService;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() throws Exception {
        TestDatabaseHelper.setupTestDatabase();
        service = new PurchaseTransactionService();
        inventoryService = new InventoryItemService();
        accountService = new CustomerAccountService();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setOut(originalOut);
        TestDatabaseHelper.cleanupTestDatabase();
        TestDatabaseHelper.restoreOriginalDatabase();
    }

    private int createTestCustomer() {
        String uniqueUsername = "testuser_" + System.nanoTime();
        accountService.createCustomerAccount(uniqueUsername, "pass");
        CustomerAccount account = accountService.authenticateUser(uniqueUsername, "pass");
        return account.getAccountId();
    }

    private List<ShoppingCartEntry> createTestCart() {
        inventoryService.insertInventoryItem("Product 1", "Desc 1", 10.00, 20);
        inventoryService.insertInventoryItem("Product 2", "Desc 2", 20.00, 15);
        
        List<InventoryItem> items = inventoryService.retrieveAllInventoryItems();
        List<ShoppingCartEntry> cart = new ArrayList<>();
        cart.add(new ShoppingCartEntry(items.get(0), 2));
        cart.add(new ShoppingCartEntry(items.get(1), 3));
        return cart;
    }

    @Test
    void testCreatePurchaseTransaction_Success() {
        int accountId = createTestCustomer();
        List<ShoppingCartEntry> cart = createTestCart();
        outContent.reset();
        
        int transactionId = service.createPurchaseTransaction(accountId, cart, "ONLINE");
        assertTrue(transactionId > 0);
        assertTrue(outContent.toString().contains("Order placed successfully!"));
    }

    @Test
    void testCreatePurchaseTransaction_AllPaymentTypes() {
        int accountId = createTestCustomer();
        List<ShoppingCartEntry> cart = createTestCart();
        
        int id1 = service.createPurchaseTransaction(accountId, cart, "ONLINE");
        int id2 = service.createPurchaseTransaction(accountId, cart, "CARD");
        int id3 = service.createPurchaseTransaction(accountId, cart, "COD");
        
        assertTrue(id1 > 0);
        assertTrue(id2 > 0);
        assertTrue(id3 > 0);
    }

    @Test
    void testCreatePurchaseTransaction_CalculatesTotal() {
        int accountId = createTestCustomer();
        List<ShoppingCartEntry> cart = createTestCart();
        // Expected total: (10.00 * 2) + (20.00 * 3) = 20.00 + 60.00 = 80.00
        
        int transactionId = service.createPurchaseTransaction(accountId, cart, "ONLINE");
        PurchaseTransaction transaction = service.fetchPurchaseTransactionById(transactionId);
        assertEquals(80.00, transaction.getTransactionTotal(), 0.01);
    }

    @Test
    void testApprovePurchaseTransaction_Success() {
        int accountId = createTestCustomer();
        List<ShoppingCartEntry> cart = createTestCart();
        int transactionId = service.createPurchaseTransaction(accountId, cart, "ONLINE");
        outContent.reset();
        
        boolean result = service.approvePurchaseTransaction(transactionId);
        assertTrue(result);
        assertTrue(outContent.toString().contains("ACCEPTED"));
        
        PurchaseTransaction transaction = service.fetchPurchaseTransactionById(transactionId);
        assertEquals("ACCEPTED", transaction.getTransactionStatus());
    }

    @Test
    void testApprovePurchaseTransaction_DeductsInventory() {
        int accountId = createTestCustomer();
        inventoryService.insertInventoryItem("Stock Product", "Desc", 10.00, 100);
        List<InventoryItem> items = inventoryService.retrieveAllInventoryItems();
        List<ShoppingCartEntry> cart = new ArrayList<>();
        cart.add(new ShoppingCartEntry(items.get(0), 5));
        
        int transactionId = service.createPurchaseTransaction(accountId, cart, "ONLINE");
        service.approvePurchaseTransaction(transactionId);
        
        InventoryItem updated = inventoryService.fetchInventoryItemById(items.get(0).getItemId());
        assertEquals(95, updated.getItemStock());
    }

    @Test
    void testApprovePurchaseTransaction_InsufficientStock() {
        int accountId = createTestCustomer();
        inventoryService.insertInventoryItem("Low Stock", "Desc", 10.00, 5);
        List<InventoryItem> items = inventoryService.retrieveAllInventoryItems();
        List<ShoppingCartEntry> cart = new ArrayList<>();
        cart.add(new ShoppingCartEntry(items.get(0), 10)); // Requesting more than available
        
        int transactionId = service.createPurchaseTransaction(accountId, cart, "ONLINE");
        outContent.reset();
        
        boolean result = service.approvePurchaseTransaction(transactionId);
        assertFalse(result);
        assertTrue(outContent.toString().contains("Insufficient stock"));
    }

    @Test
    void testApprovePurchaseTransaction_NonExistentOrder() {
        boolean result = service.approvePurchaseTransaction(99999);
        assertFalse(result);
        assertTrue(outContent.toString().contains("Order not found!"));
    }

    @Test
    void testApprovePurchaseTransaction_WrongStatus() {
        int accountId = createTestCustomer();
        List<ShoppingCartEntry> cart = createTestCart();
        int transactionId = service.createPurchaseTransaction(accountId, cart, "ONLINE");
        service.approvePurchaseTransaction(transactionId);
        outContent.reset();
        
        // Try to approve an already accepted order
        boolean result = service.approvePurchaseTransaction(transactionId);
        assertFalse(result);
        assertTrue(outContent.toString().contains("Order cannot be accepted"));
    }

    @Test
    void testDeclinePurchaseTransaction_Success() {
        int accountId = createTestCustomer();
        List<ShoppingCartEntry> cart = createTestCart();
        int transactionId = service.createPurchaseTransaction(accountId, cart, "ONLINE");
        outContent.reset();
        
        boolean result = service.declinePurchaseTransaction(transactionId);
        assertTrue(result);
        assertTrue(outContent.toString().contains("REJECTED"));
        
        PurchaseTransaction transaction = service.fetchPurchaseTransactionById(transactionId);
        assertEquals("REJECTED", transaction.getTransactionStatus());
    }

    @Test
    void testDeclinePurchaseTransaction_NonExistentOrder() {
        boolean result = service.declinePurchaseTransaction(99999);
        assertFalse(result);
        assertTrue(outContent.toString().contains("Order not found!"));
    }

    @Test
    void testDeclinePurchaseTransaction_WrongStatus() {
        int accountId = createTestCustomer();
        List<ShoppingCartEntry> cart = createTestCart();
        int transactionId = service.createPurchaseTransaction(accountId, cart, "ONLINE");
        service.approvePurchaseTransaction(transactionId);
        outContent.reset();
        
        // Try to reject an already accepted order
        boolean result = service.declinePurchaseTransaction(transactionId);
        assertFalse(result);
        assertTrue(outContent.toString().contains("Order cannot be rejected"));
    }

    @Test
    void testMarkAsDelivered_Success() {
        int accountId = createTestCustomer();
        List<ShoppingCartEntry> cart = createTestCart();
        int transactionId = service.createPurchaseTransaction(accountId, cart, "ONLINE");
        service.approvePurchaseTransaction(transactionId);
        outContent.reset();
        
        boolean result = service.markAsDelivered(transactionId);
        assertTrue(result);
        assertTrue(outContent.toString().contains("DELIVERED"));
        
        PurchaseTransaction transaction = service.fetchPurchaseTransactionById(transactionId);
        assertEquals("DELIVERED", transaction.getTransactionStatus());
    }

    @Test
    void testMarkAsDelivered_NonExistentOrder() {
        boolean result = service.markAsDelivered(99999);
        assertFalse(result);
        assertTrue(outContent.toString().contains("Order not found!"));
    }

    @Test
    void testMarkAsDelivered_WrongStatus() {
        int accountId = createTestCustomer();
        List<ShoppingCartEntry> cart = createTestCart();
        int transactionId = service.createPurchaseTransaction(accountId, cart, "ONLINE");
        outContent.reset();
        
        // Try to mark a CREATED order as delivered (should be ACCEPTED first)
        boolean result = service.markAsDelivered(transactionId);
        assertFalse(result);
        assertTrue(outContent.toString().contains("Only ACCEPTED orders can be marked as DELIVERED"));
    }

    @Test
    void testRetrieveAllPurchaseTransactions_Empty() {
        List<PurchaseTransaction> transactions = service.retrieveAllPurchaseTransactions();
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }

    @Test
    void testRetrieveAllPurchaseTransactions_MultipleTransactions() {
        int accountId = createTestCustomer();
        List<ShoppingCartEntry> cart = createTestCart();
        service.createPurchaseTransaction(accountId, cart, "ONLINE");
        service.createPurchaseTransaction(accountId, cart, "CARD");
        service.createPurchaseTransaction(accountId, cart, "COD");
        
        List<PurchaseTransaction> transactions = service.retrieveAllPurchaseTransactions();
        assertEquals(3, transactions.size());
    }

    @Test
    void testFetchUserPurchaseTransactions_Success() {
        int accountId1 = createTestCustomer();
        String uniqueUsername2 = "user2_" + System.nanoTime();
        accountService.createCustomerAccount(uniqueUsername2, "pass");
        CustomerAccount account2 = accountService.authenticateUser(uniqueUsername2, "pass");
        int accountId2 = account2.getAccountId();
        
        List<ShoppingCartEntry> cart = createTestCart();
        service.createPurchaseTransaction(accountId1, cart, "ONLINE");
        service.createPurchaseTransaction(accountId1, cart, "CARD");
        service.createPurchaseTransaction(accountId2, cart, "COD");
        
        List<PurchaseTransaction> user1Transactions = service.fetchUserPurchaseTransactions(accountId1);
        assertEquals(2, user1Transactions.size());
        
        List<PurchaseTransaction> user2Transactions = service.fetchUserPurchaseTransactions(accountId2);
        assertEquals(1, user2Transactions.size());
    }

    @Test
    void testFetchUserPurchaseTransactions_Empty() {
        int accountId = createTestCustomer();
        List<PurchaseTransaction> transactions = service.fetchUserPurchaseTransactions(accountId);
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }

    @Test
    void testFetchPurchaseTransactionById_Success() {
        int accountId = createTestCustomer();
        List<ShoppingCartEntry> cart = createTestCart();
        int transactionId = service.createPurchaseTransaction(accountId, cart, "ONLINE");
        
        PurchaseTransaction found = service.fetchPurchaseTransactionById(transactionId);
        assertNotNull(found);
        assertEquals(transactionId, found.getTransactionId());
        assertEquals(accountId, found.getCustomerAccountId());
        assertEquals("CREATED", found.getTransactionStatus());
    }

    @Test
    void testFetchPurchaseTransactionById_NonExistent() {
        PurchaseTransaction found = service.fetchPurchaseTransactionById(99999);
        assertNull(found);
    }
}

