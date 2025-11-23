package org.example.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TestDatabaseHelper {
    public static Connection getTestConnection() throws SQLException {
        return DataConnectionHelper.getConnection();
    }

    public static void setupTestDatabase() throws Exception {
        // Set system property to enable test mode (uses in-memory database)
        System.setProperty("test.mode", "true");
        // Clean up first to ensure fresh start
        cleanupTestDatabase();
        initializeTestDatabase();
    }

    public static void restoreOriginalDatabase() throws Exception {
        // Remove test mode system property
        System.clearProperty("test.mode");
    }

    public static void initializeTestDatabase() throws SQLException {
        try (Connection conn = getTestConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS payments");
            stmt.execute("DROP TABLE IF EXISTS order_items");
            stmt.execute("DROP TABLE IF EXISTS orders");
            stmt.execute("DROP TABLE IF EXISTS products");
            stmt.execute("DROP TABLE IF EXISTS users");

            stmt.execute("""
                CREATE TABLE users (
                    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password VARCHAR(100) NOT NULL,
                    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'CUSTOMER'))
                )
            """);

            stmt.execute("""
                CREATE TABLE products (
                    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    name VARCHAR(200) NOT NULL,
                    description TEXT,
                    price DECIMAL(10, 2) NOT NULL,
                    stock INT NOT NULL DEFAULT 0
                )
            """);

            stmt.execute("""
                CREATE TABLE orders (
                    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    user_id INT NOT NULL,
                    status VARCHAR(20) NOT NULL CHECK (status IN ('CREATED', 'ACCEPTED', 'REJECTED', 'DELIVERED')),
                    order_date TIMESTAMP NOT NULL,
                    total_amount DECIMAL(10, 2) NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE order_items (
                    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    order_id INT NOT NULL,
                    product_id INT NOT NULL,
                    quantity INT NOT NULL,
                    price DECIMAL(10, 2) NOT NULL,
                    FOREIGN KEY (order_id) REFERENCES orders(id),
                    FOREIGN KEY (product_id) REFERENCES products(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE payments (
                    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    order_id INT NOT NULL,
                    payment_method VARCHAR(20) NOT NULL CHECK (payment_method IN ('ONLINE', 'CARD', 'COD')),
                    status VARCHAR(20) NOT NULL CHECK (status IN ('SUCCESS', 'FAILED', 'REFUNDED')),
                    amount DECIMAL(10, 2) NOT NULL,
                    payment_date TIMESTAMP NOT NULL,
                    FOREIGN KEY (order_id) REFERENCES orders(id)
                )
            """);
        }
    }

    public static void cleanupTestDatabase() throws SQLException {
        try (Connection conn = getTestConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS payments");
            stmt.execute("DROP TABLE IF EXISTS order_items");
            stmt.execute("DROP TABLE IF EXISTS orders");
            stmt.execute("DROP TABLE IF EXISTS products");
            stmt.execute("DROP TABLE IF EXISTS users");
        }
    }
}

