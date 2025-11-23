package org.example.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.*;

class DataConnectionHelperTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testGetConnection_Success() throws Exception {
        Connection conn = DataConnectionHelper.getConnection();
        assertNotNull(conn);
        assertFalse(conn.isClosed());
        conn.close();
    }

    @Test
    void testGetConnection_MultipleConnections() throws Exception {
        Connection conn1 = DataConnectionHelper.getConnection();
        Connection conn2 = DataConnectionHelper.getConnection();
        
        assertNotNull(conn1);
        assertNotNull(conn2);
        assertNotSame(conn1, conn2);
        
        conn1.close();
        conn2.close();
    }

    @Test
    void testInitializeDatabase_Success() throws Exception {
        System.setProperty("test.mode", "true");
        try {
            DataConnectionHelper.initializeDatabase();
            assertTrue(outContent.toString().contains("Database initialized successfully!"));
            
            // Verify tables were created
            try (Connection conn = DataConnectionHelper.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // Test users table
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
                rs.next();
                assertTrue(rs.getInt(1) >= 0);
                
                // Test products table
                rs = stmt.executeQuery("SELECT COUNT(*) FROM products");
                rs.next();
                assertTrue(rs.getInt(1) >= 0);
                
                // Test orders table
                rs = stmt.executeQuery("SELECT COUNT(*) FROM orders");
                rs.next();
                assertTrue(rs.getInt(1) >= 0);
                
                // Test order_items table
                rs = stmt.executeQuery("SELECT COUNT(*) FROM order_items");
                rs.next();
                assertTrue(rs.getInt(1) >= 0);
                
                // Test payments table
                rs = stmt.executeQuery("SELECT COUNT(*) FROM payments");
                rs.next();
                assertTrue(rs.getInt(1) >= 0);
            }
        } finally {
            System.clearProperty("test.mode");
        }
    }

    @Test
    void testInitializeDatabase_CreatesAdminAccount() throws Exception {
        System.setProperty("test.mode", "true");
        try {
            // Clean up first
            try (Connection conn = DataConnectionHelper.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS payments");
                stmt.execute("DROP TABLE IF EXISTS order_items");
                stmt.execute("DROP TABLE IF EXISTS orders");
                stmt.execute("DROP TABLE IF EXISTS products");
                stmt.execute("DROP TABLE IF EXISTS users");
            }
            outContent.reset();
            
            DataConnectionHelper.initializeDatabase();
            assertTrue(outContent.toString().contains("Default admin account created"));
            
            // Verify admin account exists
            try (Connection conn = DataConnectionHelper.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE username = 'admin'")) {
                
                assertTrue(rs.next());
                assertEquals("admin", rs.getString("username"));
                assertEquals("admin123", rs.getString("password"));
                assertEquals("ADMIN", rs.getString("role"));
            }
        } finally {
            System.clearProperty("test.mode");
        }
    }

    @Test
    void testInitializeDatabase_DoesNotDuplicateAdmin() throws Exception {
        System.setProperty("test.mode", "true");
        try {
            DataConnectionHelper.initializeDatabase();
            outContent.reset();
            
            // Initialize again - should not create duplicate admin
            DataConnectionHelper.initializeDatabase();
            
            // Count admin accounts
            try (Connection conn = DataConnectionHelper.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE username = 'admin'")) {
                
                rs.next();
                assertEquals(1, rs.getInt(1));
            }
        } finally {
            System.clearProperty("test.mode");
        }
    }

    @Test
    void testInitializeDatabase_CreatesAllTables() throws Exception {
        System.setProperty("test.mode", "true");
        try {
            DataConnectionHelper.initializeDatabase();
            
            try (Connection conn = DataConnectionHelper.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // Verify all tables exist by querying them
                assertDoesNotThrow(() -> stmt.executeQuery("SELECT * FROM users"));
                assertDoesNotThrow(() -> stmt.executeQuery("SELECT * FROM products"));
                assertDoesNotThrow(() -> stmt.executeQuery("SELECT * FROM orders"));
                assertDoesNotThrow(() -> stmt.executeQuery("SELECT * FROM order_items"));
                assertDoesNotThrow(() -> stmt.executeQuery("SELECT * FROM payments"));
            }
        } finally {
            System.clearProperty("test.mode");
        }
    }

    @Test
    void testGetConnection_CanExecuteQueries() throws Exception {
        System.setProperty("test.mode", "true");
        try {
            DataConnectionHelper.initializeDatabase();
            
            try (Connection conn = DataConnectionHelper.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // Insert a test record
                stmt.executeUpdate("INSERT INTO users (username, password, role) VALUES ('testuser', 'testpass', 'CUSTOMER')");
                
                // Query it back
                ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE username = 'testuser'");
                assertTrue(rs.next());
                assertEquals("testuser", rs.getString("username"));
            }
        } finally {
            System.clearProperty("test.mode");
        }
    }
}

