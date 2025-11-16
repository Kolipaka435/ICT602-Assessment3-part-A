package org.example.dao;

import org.example.model.InventoryItem;
import org.example.util.DataConnectionHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// This Data Access Object (DAO) class handles all database operations related to products
// It provides methods for adding, updating, deleting, and querying products in the inventory
public class InventoryItemDAO {
    // Adds a new product to the database and returns the generated product ID
    // Returns the product ID if successful, -1 if there was an error during insertion
    public int insertRecord(InventoryItem item) {
        String sql = "INSERT INTO products (name, description, price, stock) VALUES (?, ?, ?, ?)";
        try (Connection conn = DataConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, item.getItemName()); // Set the product name parameter in the SQL query
            pstmt.setString(2, item.getItemDescription()); // Set the product description parameter
            pstmt.setDouble(3, item.getItemPrice()); // Set the product price parameter
            pstmt.setInt(4, item.getItemStock()); // Set the product stock quantity parameter
            pstmt.executeUpdate(); // Execute the insert statement
            ResultSet rs = pstmt.getGeneratedKeys(); // Retrieve the auto-generated product ID
            if (rs.next()) {
                return rs.getInt(1); // Return the generated product ID
            }
        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage()); // Log any database errors
        }
        return -1; // Return -1 if product insertion failed
    }

    // Updates an existing product in the database with new information
    // Returns true if the update was successful, false if the product was not found or an error occurred
    public boolean modifyRecord(InventoryItem item) {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, stock = ? WHERE id = ?";
        try (Connection conn = DataConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getItemName()); // Set the new product name
            pstmt.setString(2, item.getItemDescription()); // Set the new product description
            pstmt.setDouble(3, item.getItemPrice()); // Set the new product price
            pstmt.setInt(4, item.getItemStock()); // Set the new stock quantity
            pstmt.setInt(5, item.getItemId()); // Set the product ID for the WHERE clause
            return pstmt.executeUpdate() > 0; // Return true if at least one row was updated
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage()); // Log any database errors
            return false;
        }
    }

    // Deletes a product from the database by its ID
    // Returns true if the product was successfully deleted, false if it was not found or an error occurred
    public boolean removeRecord(int itemId) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DataConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId); // Set the product ID parameter for the WHERE clause
            return pstmt.executeUpdate() > 0; // Return true if at least one row was deleted
        } catch (SQLException e) {
            System.err.println("Error deleting product: " + e.getMessage()); // Log any database errors
            return false;
        }
    }

    // Retrieves all products from the database and returns them as a list
    // Products are ordered by ID in ascending order
    public List<InventoryItem> retrieveAllRecords() {
        List<InventoryItem> items = new ArrayList<>(); // Create a list to store the retrieved products
        String sql = "SELECT * FROM products ORDER BY id";
        try (Connection conn = DataConnectionHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // Iterate through all rows in the result set and create InventoryItem objects
            while (rs.next()) {
                items.add(new InventoryItem(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getInt("stock")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error finding products: " + e.getMessage()); // Log any database errors
        }
        return items; // Return the list of products, which may be empty if no products exist
    }

    // Retrieves a single product from the database by its unique ID
    // Returns the InventoryItem object if found, null if the product does not exist
    public InventoryItem locateById(int itemId) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DataConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId); // Set the product ID parameter for the WHERE clause
            ResultSet rs = pstmt.executeQuery(); // Execute the query and get the result set
            if (rs.next()) {
                // If a product is found, create and return an InventoryItem object with the retrieved data
                return new InventoryItem(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getInt("stock")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error finding product: " + e.getMessage()); // Log any database errors
        }
        return null; // Return null if no product is found with the given ID
    }

    // Updates the stock quantity of a product by deducting the specified quantity
    // This is used when an order is accepted to reduce inventory levels
    // Returns true if the stock update was successful, false if an error occurred
    public boolean adjustStockLevel(int itemId, int quantity) {
        String sql = "UPDATE products SET stock = stock - ? WHERE id = ?";
        try (Connection conn = DataConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantity); // Set the quantity to deduct from stock
            pstmt.setInt(2, itemId); // Set the product ID for the WHERE clause
            return pstmt.executeUpdate() > 0; // Return true if at least one row was updated
        } catch (SQLException e) {
            System.err.println("Error updating stock: " + e.getMessage()); // Log any database errors
            return false;
        }
    }
}
