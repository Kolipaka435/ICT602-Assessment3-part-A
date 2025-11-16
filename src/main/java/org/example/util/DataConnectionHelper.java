package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// This utility class provides database connection and initialization functionality
// It manages the H2 database connection and creates all necessary tables for the application
public class DataConnectionHelper {
    // Database connection URL for H2 database, stored in the current directory as fers_db
    // AUTO_SERVER=TRUE allows multiple connections to the same database file
    private static final String DB_URL = "jdbc:h2:./fers_db;AUTO_SERVER=TRUE";
    // Default H2 database username for authentication
    private static final String DB_USER = "sa";
    // Default H2 database password, empty for this application
    private static final String DB_PASSWORD = "";

    // Creates and returns a connection to the H2 database
    // This method is used by all DAO classes to establish database connections
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    // Initializes the database by creating all necessary tables if they don't already exist
    // Also creates a default admin account if no admin user exists in the system
    // This method should be called once at application startup before any database operations
    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create Users table to store all user accounts including admins and customers
            // The table includes username, password, and role with constraints to ensure data integrity
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password VARCHAR(100) NOT NULL,
                    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'CUSTOMER'))
                )
            """);

            // Create Products table to store all products available in the inventory
            // Stores product information including name, description, price, and current stock level
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    name VARCHAR(200) NOT NULL,
                    description TEXT,
                    price DECIMAL(10, 2) NOT NULL,
                    stock INT NOT NULL DEFAULT 0
                )
            """);

            // Create Orders table to store all orders placed by customers
            // Links to users table and tracks order status through the fulfillment lifecycle
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    user_id INT NOT NULL,
                    status VARCHAR(20) NOT NULL CHECK (status IN ('CREATED', 'ACCEPTED', 'REJECTED', 'DELIVERED')),
                    order_date TIMESTAMP NOT NULL,
                    total_amount DECIMAL(10, 2) NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            // Create Order Items table to store individual products within each order
            // Links to both orders and products tables, storing quantity and price at time of order
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS order_items (
                    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    order_id INT NOT NULL,
                    product_id INT NOT NULL,
                    quantity INT NOT NULL,
                    price DECIMAL(10, 2) NOT NULL,
                    FOREIGN KEY (order_id) REFERENCES orders(id),
                    FOREIGN KEY (product_id) REFERENCES products(id)
                )
            """);

            // Create Payments table to store payment transactions associated with orders
            // Tracks payment method, status, amount, and timestamp for financial record keeping
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS payments (
                    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    order_id INT NOT NULL,
                    payment_method VARCHAR(20) NOT NULL CHECK (payment_method IN ('ONLINE', 'CARD', 'COD')),
                    status VARCHAR(20) NOT NULL CHECK (status IN ('SUCCESS', 'FAILED', 'REFUNDED')),
                    amount DECIMAL(10, 2) NOT NULL,
                    payment_date TIMESTAMP NOT NULL,
                    FOREIGN KEY (order_id) REFERENCES orders(id)
                )
            """);

            // Create default admin account if it doesn't exist in the database
            // This ensures there is always at least one admin user to manage the system
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE username = 'admin'");
            rs.next();
            if (rs.getInt(1) == 0) {
                // Insert the default admin account with username 'admin' and password 'admin123'
                stmt.execute("INSERT INTO users (username, password, role) VALUES ('admin', 'admin123', 'ADMIN')");
                System.out.println("Default admin account created: username='admin', password='admin123'");
            }

            System.out.println("Database initialized successfully!"); // Confirm successful database setup
        } catch (SQLException e) {
            // Handle any database errors that occur during initialization
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

