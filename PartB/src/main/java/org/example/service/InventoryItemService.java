package org.example.service;

import org.example.dao.InventoryItemDAO;
import org.example.model.InventoryItem;

import java.util.List;

// This service class provides business logic for product-related operations
// It acts as an intermediary between the presentation layer and the data access layer
public class InventoryItemService {
    // Data Access Object for performing database operations on products
    private final InventoryItemDAO inventoryDAO;

    // Constructor that initializes the InventoryItemDAO for database operations
    public InventoryItemService() {
        this.inventoryDAO = new InventoryItemDAO();
    }

    // Adds a new product to the inventory with the specified details
    // Creates an InventoryItem object and saves it to the database through the DAO
    // Returns true if the product was successfully added, false if there was an error
    public boolean insertInventoryItem(String itemName, String itemDescription, double itemPrice, int itemStock) {
        InventoryItem item = new InventoryItem(itemName, itemDescription, itemPrice, itemStock); // Create a new product object
        int itemId = inventoryDAO.insertRecord(item); // Save the product to the database and get the generated ID
        if (itemId > 0) {
            System.out.println("Product added successfully with ID: " + itemId); // Confirm successful addition
            return true;
        } else {
            System.out.println("Failed to add product!"); // Inform user of failure
            return false;
        }
    }

    // Updates an existing product in the inventory with new information
    // Creates an InventoryItem object with the updated details and saves it to the database
    // Returns true if the product was successfully updated, false if the product was not found or an error occurred
    public boolean modifyInventoryItem(int itemId, String itemName, String itemDescription, double itemPrice, int itemStock) {
        InventoryItem item = new InventoryItem(itemId, itemName, itemDescription, itemPrice, itemStock); // Create a product object with updated data
        boolean success = inventoryDAO.modifyRecord(item); // Update the product in the database
        if (success) {
            System.out.println("Product updated successfully!"); // Confirm successful update
        } else {
            System.out.println("Failed to update product!"); // Inform user of failure
        }
        return success;
    }

    // Deletes a product from the inventory by its ID
    // Returns true if the product was successfully deleted, false if the product was not found or an error occurred
    public boolean removeInventoryItem(int itemId) {
        boolean success = inventoryDAO.removeRecord(itemId); // Delete the product from the database
        if (success) {
            System.out.println("Product deleted successfully!"); // Confirm successful deletion
        } else {
            System.out.println("Failed to delete product!"); // Inform user of failure
        }
        return success;
    }

    // Retrieves all products from the inventory
    // Returns a list of all InventoryItem objects currently in the database
    public List<InventoryItem> retrieveAllInventoryItems() {
        return inventoryDAO.retrieveAllRecords(); // Delegate to the DAO to retrieve all products
    }

    // Retrieves a single product from the inventory by its unique ID
    // Returns the InventoryItem object if found, null if the product does not exist
    public InventoryItem fetchInventoryItemById(int itemId) {
        return inventoryDAO.locateById(itemId); // Delegate to the DAO to retrieve the product
    }

    // Updates the stock quantity of a product by deducting the specified quantity
    // This is used when an order is accepted to reduce inventory levels
    // Returns true if the stock update was successful, false if an error occurred
    public boolean adjustStockLevel(int itemId, int quantity) {
        return inventoryDAO.adjustStockLevel(itemId, quantity); // Delegate to the DAO to update stock
    }
}
