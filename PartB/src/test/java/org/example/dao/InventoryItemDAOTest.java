package org.example.dao;

import org.example.model.InventoryItem;
import org.example.util.TestDatabaseHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InventoryItemDAOTest {
    private InventoryItemDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        TestDatabaseHelper.setupTestDatabase();
        dao = new InventoryItemDAO();
    }

    @AfterEach
    void tearDown() throws Exception {
        TestDatabaseHelper.cleanupTestDatabase();
        TestDatabaseHelper.restoreOriginalDatabase();
    }

    @Test
    void testInsertRecord_Success() {
        InventoryItem item = new InventoryItem("Test Product", "Test Description", 29.99, 10);
        int result = dao.insertRecord(item);
        assertTrue(result > 0);
    }

    @Test
    void testInsertRecord_ReturnsGeneratedId() {
        InventoryItem item = new InventoryItem("Product 1", "Desc 1", 19.99, 5);
        int id1 = dao.insertRecord(item);
        
        InventoryItem item2 = new InventoryItem("Product 2", "Desc 2", 39.99, 15);
        int id2 = dao.insertRecord(item2);
        
        assertTrue(id1 > 0);
        assertTrue(id2 > 0);
        assertNotEquals(id1, id2);
    }

    @Test
    void testModifyRecord_Success() {
        InventoryItem item = new InventoryItem("Original", "Original Desc", 10.00, 5);
        int id = dao.insertRecord(item);
        item.setItemId(id);
        
        item.setItemName("Updated");
        item.setItemDescription("Updated Desc");
        item.setItemPrice(20.00);
        item.setItemStock(10);
        
        boolean result = dao.modifyRecord(item);
        assertTrue(result);
        
        InventoryItem updated = dao.locateById(id);
        assertEquals("Updated", updated.getItemName());
        assertEquals("Updated Desc", updated.getItemDescription());
        assertEquals(20.00, updated.getItemPrice());
        assertEquals(10, updated.getItemStock());
    }

    @Test
    void testModifyRecord_NonExistent() {
        InventoryItem item = new InventoryItem(99999, "NonExistent", "Desc", 10.00, 5);
        boolean result = dao.modifyRecord(item);
        assertFalse(result);
    }

    @Test
    void testRemoveRecord_Success() {
        InventoryItem item = new InventoryItem("ToDelete", "Desc", 15.00, 8);
        int id = dao.insertRecord(item);
        
        boolean result = dao.removeRecord(id);
        assertTrue(result);
        
        InventoryItem deleted = dao.locateById(id);
        assertNull(deleted);
    }

    @Test
    void testRemoveRecord_NonExistent() {
        boolean result = dao.removeRecord(99999);
        assertFalse(result);
    }

    @Test
    void testRetrieveAllRecords_Empty() {
        List<InventoryItem> items = dao.retrieveAllRecords();
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    void testRetrieveAllRecords_MultipleItems() {
        dao.insertRecord(new InventoryItem("Item 1", "Desc 1", 10.00, 5));
        dao.insertRecord(new InventoryItem("Item 2", "Desc 2", 20.00, 10));
        dao.insertRecord(new InventoryItem("Item 3", "Desc 3", 30.00, 15));
        
        List<InventoryItem> items = dao.retrieveAllRecords();
        assertEquals(3, items.size());
    }

    @Test
    void testRetrieveAllRecords_OrderedById() {
        int id1 = dao.insertRecord(new InventoryItem("Item 1", "Desc 1", 10.00, 5));
        int id2 = dao.insertRecord(new InventoryItem("Item 2", "Desc 2", 20.00, 10));
        int id3 = dao.insertRecord(new InventoryItem("Item 3", "Desc 3", 30.00, 15));
        
        List<InventoryItem> items = dao.retrieveAllRecords();
        assertEquals(id1, items.get(0).getItemId());
        assertEquals(id2, items.get(1).getItemId());
        assertEquals(id3, items.get(2).getItemId());
    }

    @Test
    void testLocateById_Success() {
        InventoryItem item = new InventoryItem("FindMe", "Description", 25.50, 12);
        int id = dao.insertRecord(item);
        
        InventoryItem found = dao.locateById(id);
        assertNotNull(found);
        assertEquals(id, found.getItemId());
        assertEquals("FindMe", found.getItemName());
        assertEquals("Description", found.getItemDescription());
        assertEquals(25.50, found.getItemPrice());
        assertEquals(12, found.getItemStock());
    }

    @Test
    void testLocateById_NonExistent() {
        InventoryItem found = dao.locateById(99999);
        assertNull(found);
    }

    @Test
    void testAdjustStockLevel_Success() {
        InventoryItem item = new InventoryItem("StockItem", "Desc", 10.00, 100);
        int id = dao.insertRecord(item);
        
        boolean result = dao.adjustStockLevel(id, 30);
        assertTrue(result);
        
        InventoryItem updated = dao.locateById(id);
        assertEquals(70, updated.getItemStock());
    }

    @Test
    void testAdjustStockLevel_MultipleAdjustments() {
        InventoryItem item = new InventoryItem("StockItem", "Desc", 10.00, 100);
        int id = dao.insertRecord(item);
        
        dao.adjustStockLevel(id, 20);
        dao.adjustStockLevel(id, 15);
        
        InventoryItem updated = dao.locateById(id);
        assertEquals(65, updated.getItemStock());
    }

    @Test
    void testAdjustStockLevel_NonExistent() {
        boolean result = dao.adjustStockLevel(99999, 10);
        assertFalse(result);
    }
}

