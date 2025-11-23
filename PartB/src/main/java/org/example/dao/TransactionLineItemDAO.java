package org.example.dao;

import org.example.model.TransactionLineItem;
import org.example.util.DataConnectionHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// This Data Access Object (DAO) class handles all database operations related to order items
// Order items represent individual products within an order, storing quantity and price at time of purchase
public class TransactionLineItemDAO {
    // Adds a new order item to the database, representing a product within an order
    // Returns true if the order item was successfully added, false if there was an error
    public boolean insertRecord(TransactionLineItem lineItem) {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (Connection conn = DataConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, lineItem.getTransactionId()); // Set the order ID that this item belongs to
            pstmt.setInt(2, lineItem.getInventoryItemId()); // Set the product ID that was ordered
            pstmt.setInt(3, lineItem.getItemQuantity()); // Set the quantity of the product ordered
            pstmt.setDouble(4, lineItem.getItemPriceAtPurchase()); // Set the price of the product at time of order
            return pstmt.executeUpdate() > 0; // Return true if at least one row was inserted
        } catch (SQLException e) {
            System.err.println("Error adding order item: " + e.getMessage()); // Log any database errors
            return false;
        }
    }

    // Retrieves all order items associated with a specific order, identified by order ID
    // Returns a list of TransactionLineItem objects representing all products in that order
    public List<TransactionLineItem> locateByTransactionId(int transactionId) {
        List<TransactionLineItem> lineItems = new ArrayList<>(); // Create a list to store the retrieved order items
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        try (Connection conn = DataConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transactionId); // Set the order ID parameter for the WHERE clause
            ResultSet rs = pstmt.executeQuery(); // Execute the query and get the result set
            // Iterate through all rows in the result set and create TransactionLineItem objects
            while (rs.next()) {
                lineItems.add(new TransactionLineItem(
                    rs.getInt("id"),
                    rs.getInt("order_id"),
                    rs.getInt("product_id"),
                    rs.getInt("quantity"),
                    rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error finding order items: " + e.getMessage()); // Log any database errors
        }
        return lineItems; // Return the list of order items, which may be empty if the order has no items
    }
}
