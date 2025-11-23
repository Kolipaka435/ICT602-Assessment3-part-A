package org.example.dao;

import org.example.model.CustomerAccount;
import org.example.util.DataConnectionHelper;

import java.sql.*;

// This Data Access Object (DAO) class handles all database operations related to users
// It provides methods for user registration, authentication, and user data retrieval
public class CustomerAccountDAO {
    // Registers a new user in the database by inserting their username, password, and role
    // Returns true if the registration was successful, false if there was an error
    public boolean insertRecord(CustomerAccount account) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DataConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, account.getAccountName()); // Set the username parameter in the SQL query
            pstmt.setString(2, account.getAccountPassword()); // Set the password parameter in the SQL query
            pstmt.setString(3, account.getAccountRole()); // Set the role parameter in the SQL query
            return pstmt.executeUpdate() > 0; // Return true if at least one row was inserted
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage()); // Log any database errors
            return false;
        }
    }

    // Authenticates a user by checking if the provided username and password match a record in the database
    // Returns the CustomerAccount object if authentication succeeds, null if credentials are invalid
    public CustomerAccount authenticateUser(String accountName, String accountPassword) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DataConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountName); // Set the username parameter for the WHERE clause
            pstmt.setString(2, accountPassword); // Set the password parameter for the WHERE clause
            ResultSet rs = pstmt.executeQuery(); // Execute the query and get the result set
            if (rs.next()) {
                // If a matching user is found, create and return a CustomerAccount object with the retrieved data
                return new CustomerAccount(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error logging in: " + e.getMessage()); // Log any database errors
        }
        return null; // Return null if no matching user is found
    }

    // Retrieves a user from the database by their unique ID
    // Returns the CustomerAccount object if found, null if the user does not exist
    public CustomerAccount locateById(int accountId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DataConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId); // Set the user ID parameter for the WHERE clause
            ResultSet rs = pstmt.executeQuery(); // Execute the query and get the result set
            if (rs.next()) {
                // If a user is found, create and return a CustomerAccount object with the retrieved data
                return new CustomerAccount(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage()); // Log any database errors
        }
        return null; // Return null if no user is found with the given ID
    }

    // Checks if a username already exists in the database
    // Returns true if the username is already taken, false if it is available
    public boolean checkUsernameAvailability(String accountName) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DataConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountName); // Set the username parameter for the WHERE clause
            ResultSet rs = pstmt.executeQuery(); // Execute the query and get the result set
            if (rs.next()) {
                return rs.getInt(1) > 0; // Return true if count is greater than 0, meaning username exists
            }
        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage()); // Log any database errors
        }
        return false; // Return false if username does not exist or if there was an error
    }
}
