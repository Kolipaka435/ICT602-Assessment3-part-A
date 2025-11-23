package org.example.service;

import org.example.model.InventoryItem;
import org.example.util.TestDatabaseHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InventoryItemServiceTest {
    private InventoryItemService service;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() throws Exception {
        TestDatabaseHelper.setupTestDatabase();
        service = new InventoryItemService();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setOut(originalOut);
        TestDatabaseHelper.cleanupTestDatabase();
        TestDatabaseHelper.restoreOriginalDatabase();
    }

    @Test
    void testInsertInventoryItem_Success() {
        boolean result = service.insertInventoryItem("Test Product", "Test Description", 29.99, 10);
        assertTrue(result);
        assertTrue(outContent.toString().contains("Product added successfully"));
    }

    @Test
    void testInsertInventoryItem_WithZeroPrice() {
        boolean result = service.insertInventoryItem("Free Product", "Free", 0.0, 5);
        assertTrue(result);
    }

    @Test
    void testInsertInventoryItem_WithZeroStock() {
        boolean result = service.insertInventoryItem("OutOfStock", "Desc", 10.00, 0);
        assertTrue(result);
    }

    @Test
    void testModifyInventoryItem_Success() {
        service.insertInventoryItem("Original", "Original Desc", 10.00, 5);
        outContent.reset();
        
        List<InventoryItem> items = service.retrieveAllInventoryItems();
        int id = items.get(0).getItemId();
        
        boolean result = service.modifyInventoryItem(id, "Updated", "Updated Desc", 20.00, 10);
        assertTrue(result);
        assertTrue(outContent.toString().contains("Product updated successfully!"));
        
        InventoryItem updated = service.fetchInventoryItemById(id);
        assertEquals("Updated", updated.getItemName());
        assertEquals("Updated Desc", updated.getItemDescription());
        assertEquals(20.00, updated.getItemPrice());
        assertEquals(10, updated.getItemStock());
    }

    @Test
    void testModifyInventoryItem_NonExistent() {
        boolean result = service.modifyInventoryItem(99999, "Name", "Desc", 10.00, 5);
        assertFalse(result);
        assertTrue(outContent.toString().contains("Failed to update product!"));
    }

    @Test
    void testRemoveInventoryItem_Success() {
        service.insertInventoryItem("ToDelete", "Desc", 15.00, 8);
        List<InventoryItem> items = service.retrieveAllInventoryItems();
        int id = items.get(0).getItemId();
        outContent.reset();
        
        boolean result = service.removeInventoryItem(id);
        assertTrue(result);
        assertTrue(outContent.toString().contains("Product deleted successfully!"));
        
        InventoryItem deleted = service.fetchInventoryItemById(id);
        assertNull(deleted);
    }

    @Test
    void testRemoveInventoryItem_NonExistent() {
        boolean result = service.removeInventoryItem(99999);
        assertFalse(result);
        assertTrue(outContent.toString().contains("Failed to delete product!"));
    }

    @Test
    void testRetrieveAllInventoryItems_Empty() {
        List<InventoryItem> items = service.retrieveAllInventoryItems();
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    void testRetrieveAllInventoryItems_MultipleItems() {
        service.insertInventoryItem("Item 1", "Desc 1", 10.00, 5);
        service.insertInventoryItem("Item 2", "Desc 2", 20.00, 10);
        service.insertInventoryItem("Item 3", "Desc 3", 30.00, 15);
        
        List<InventoryItem> items = service.retrieveAllInventoryItems();
        assertEquals(3, items.size());
    }

    @Test
    void testFetchInventoryItemById_Success() {
        service.insertInventoryItem("FindMe", "Description", 25.50, 12);
        List<InventoryItem> items = service.retrieveAllInventoryItems();
        int id = items.get(0).getItemId();
        
        InventoryItem found = service.fetchInventoryItemById(id);
        assertNotNull(found);
        assertEquals(id, found.getItemId());
        assertEquals("FindMe", found.getItemName());
        assertEquals("Description", found.getItemDescription());
        assertEquals(25.50, found.getItemPrice());
        assertEquals(12, found.getItemStock());
    }

    @Test
    void testFetchInventoryItemById_NonExistent() {
        InventoryItem found = service.fetchInventoryItemById(99999);
        assertNull(found);
    }

    @Test
    void testAdjustStockLevel_Success() {
        service.insertInventoryItem("StockItem", "Desc", 10.00, 100);
        List<InventoryItem> items = service.retrieveAllInventoryItems();
        int id = items.get(0).getItemId();
        
        boolean result = service.adjustStockLevel(id, 30);
        assertTrue(result);
        
        InventoryItem updated = service.fetchInventoryItemById(id);
        assertEquals(70, updated.getItemStock());
    }

    @Test
    void testAdjustStockLevel_MultipleAdjustments() {
        service.insertInventoryItem("StockItem", "Desc", 10.00, 100);
        List<InventoryItem> items = service.retrieveAllInventoryItems();
        int id = items.get(0).getItemId();
        
        service.adjustStockLevel(id, 20);
        service.adjustStockLevel(id, 15);
        
        InventoryItem updated = service.fetchInventoryItemById(id);
        assertEquals(65, updated.getItemStock());
    }

    @Test
    void testAdjustStockLevel_NonExistent() {
        boolean result = service.adjustStockLevel(99999, 10);
        assertFalse(result);
    }
}

