package org.example.dao;
import org.example.model.PurchaseTransaction;
import org.example.util.DataConnectionHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// This Data Access Object (DAO) class handles all database operations related to orders
// It provides methods for creating orders, updating order status, and querying order information
public class PurchaseTransactionDAO {
    // Creates a new order in the database and returns the generated order ID
    // Returns the order ID if successful, -1 if there was an error during insertion
    public int insertTransaction(PurchaseTransaction transaction) {
        String sql = "INSERT INTO orders (user_id, status, order_date, total_amount) VALUES (?, ?, ?, ?)";
        try (Connection conn = DataConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, transaction.getCustomerAccountId()); // Set the user ID who placed the order
            pstmt.setString(2, transaction.getTransactionStatus()); // Set the initial order status (typically "CREATED")
            pstmt.setTimestamp(3, Timestamp.valueOf(transaction.getTransactionDate())); // Set the order date and time
            pstmt.setDouble(4, transaction.getTransactionTotal()); // Set the total amount for the order
            pstmt.executeUpdate(); // Execute the insert statement
            ResultSet rs = pstmt.getGeneratedKeys(); // Retrieve the auto-generated order ID
            if (rs.next()) {
                return rs.getInt(1); // Return the generated order ID
            }
        } catch (SQLException e) {
            System.err.println("Error creating order: " + e.getMessage()); // Log any database errors
        }
        return -1; // Return -1 if order creation failed
    }

    // Updates the status of an existing order in the database
    // Used when orders are accepted, rejected, or marked as delivered
    // Returns true if the status update was successful, false if the order was not found or an error occurred
    public boolean modifyStatus(int transactionId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DataConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status); // Set the new order status
            pstmt.setInt(2, transactionId); // Set the order ID for the WHERE clause
            return pstmt.executeUpdate() > 0; // Return true if at least one row was updated
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage()); // Log any database errors
            return false;
        }
    }

    // Retrieves all orders from the database and returns them as a list
    // Orders are sorted by order date in descending order (most recent first)
    public List<PurchaseTransaction> retrieveAllRecords() {
        List<PurchaseTransaction> transactions = new ArrayList<>(); // Create a list to store the retrieved orders
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";
        try (Connection conn = DataConnectionHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // Iterate through all rows in the result set and create PurchaseTransaction objects
            while (rs.next()) {
                transactions.add(new PurchaseTransaction(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("status"),
                    rs.getTimestamp("order_date").toLocalDateTime(), // Convert SQL timestamp to LocalDateTime
                    rs.getDouble("total_amount")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error finding orders: " + e.getMessage()); // Log any database errors
        }
        return transactions; // Return the list of orders, which may be empty if no orders exist
    }

    // Retrieves all orders placed by a specific user, identified by their user ID
    // Orders are sorted by order date in descending order (most recent first)
    public List<PurchaseTransaction> locateByUserId(int accountId) {
        List<PurchaseTransaction> transactions = new ArrayList<>(); // Create a list to store the retrieved orders
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        try (Connection conn = DataConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId); // Set the user ID parameter for the WHERE clause
            ResultSet rs = pstmt.executeQuery(); // Execute the query and get the result set
            // Iterate through all rows in the result set and create PurchaseTransaction objects
            while (rs.next()) {
                transactions.add(new PurchaseTransaction(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("status"),
                    rs.getTimestamp("order_date").toLocalDateTime(), // Convert SQL timestamp to LocalDateTime
                    rs.getDouble("total_amount")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error finding user orders: " + e.getMessage()); // Log any database errors
        }
        return transactions; // Return the list of orders for the user, which may be empty
    }

    // Retrieves a single order from the database by its unique ID
    // Returns the PurchaseTransaction object if found, null if the order does not exist
    public PurchaseTransaction locateById(int transactionId) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (Connection conn = DataConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transactionId); // Set the order ID parameter for the WHERE clause
            ResultSet rs = pstmt.executeQuery(); // Execute the query and get the result set
            if (rs.next()) {
                // If an order is found, create and return a PurchaseTransaction object with the retrieved data
                return new PurchaseTransaction(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("status"),
                    rs.getTimestamp("order_date").toLocalDateTime(), // Convert SQL timestamp to LocalDateTime
                    rs.getDouble("total_amount")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error finding order: " + e.getMessage()); // Log any database errors
        }
        return null; // Return null if no order is found with the given ID
    }
}
