package org.example.dao;

import org.example.model.PaymentRecord;
import org.example.util.DataConnectionHelper;

import java.sql.*;

// This Data Access Object (DAO) class handles all database operations related to payments
// It provides methods for recording payments, retrieving payment information, and updating payment status
public class PaymentRecordDAO {
    // Adds a new payment record to the database associated with an order
    // Returns true if the payment was successfully recorded, false if there was an error
    public boolean insertRecord(PaymentRecord paymentRecord) {
        String sql = "INSERT INTO payments (order_id, payment_method, status, amount, payment_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DataConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, paymentRecord.getTransactionId()); // Set the order ID that this payment is associated with
            pstmt.setString(2, paymentRecord.getPaymentType()); // Set the payment method (ONLINE, CARD, or COD)
            pstmt.setString(3, paymentRecord.getPaymentStatus()); // Set the payment status (SUCCESS, FAILED, or REFUNDED)
            pstmt.setDouble(4, paymentRecord.getPaymentAmount()); // Set the payment amount
            pstmt.setTimestamp(5, Timestamp.valueOf(paymentRecord.getPaymentTimestamp())); // Set the payment date and time
            return pstmt.executeUpdate() > 0; // Return true if at least one row was inserted
        } catch (SQLException e) {
            System.err.println("Error adding payment: " + e.getMessage()); // Log any database errors
            return false;
        }
    }

    // Retrieves the payment record associated with a specific order, identified by order ID
    // Returns the PaymentRecord object if found, null if no payment exists for that order
    public PaymentRecord locateByTransactionId(int transactionId) {
        String sql = "SELECT * FROM payments WHERE order_id = ?";
        try (Connection conn = DataConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transactionId); // Set the order ID parameter for the WHERE clause
            ResultSet rs = pstmt.executeQuery(); // Execute the query and get the result set
            if (rs.next()) {
                // If a payment is found, create and return a PaymentRecord object with the retrieved data
                return new PaymentRecord(
                    rs.getInt("id"),
                    rs.getInt("order_id"),
                    rs.getString("payment_method"),
                    rs.getString("status"),
                    rs.getDouble("amount"),
                    rs.getTimestamp("payment_date").toLocalDateTime() // Convert SQL timestamp to LocalDateTime
                );
            }
        } catch (SQLException e) {
            System.err.println("Error finding payment: " + e.getMessage()); // Log any database errors
        }
        return null; // Return null if no payment is found for the given order ID
    }

    // Updates the status of a payment associated with a specific order
    // Used when payments are refunded or when payment status needs to be changed
    // Returns true if the status update was successful, false if the payment was not found or an error occurred
    public boolean modifyPaymentStatus(int transactionId, String status) {
        String sql = "UPDATE payments SET status = ? WHERE order_id = ?";
        try (Connection conn = DataConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status); // Set the new payment status
            pstmt.setInt(2, transactionId); // Set the order ID for the WHERE clause
            return pstmt.executeUpdate() > 0; // Return true if at least one row was updated
        } catch (SQLException e) {
            System.err.println("Error updating payment status: " + e.getMessage()); // Log any database errors
            return false;
        }
    }
}
