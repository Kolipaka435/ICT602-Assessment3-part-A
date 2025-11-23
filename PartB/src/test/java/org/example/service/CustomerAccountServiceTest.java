package org.example.service;

import org.example.model.CustomerAccount;
import org.example.util.TestDatabaseHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.*;

class CustomerAccountServiceTest {
    private CustomerAccountService service;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() throws Exception {
        TestDatabaseHelper.setupTestDatabase();
        service = new CustomerAccountService();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setOut(originalOut);
        TestDatabaseHelper.cleanupTestDatabase();
        TestDatabaseHelper.restoreOriginalDatabase();
    }

    @Test
    void testCreateCustomerAccount_Success() {
        boolean result = service.createCustomerAccount("newuser", "password123");
        assertTrue(result);
        assertTrue(outContent.toString().contains("Customer registered successfully!"));
    }

    @Test
    void testCreateCustomerAccount_DuplicateUsername() {
        service.createCustomerAccount("duplicate", "pass1");
        outContent.reset();
        
        boolean result = service.createCustomerAccount("duplicate", "pass2");
        assertFalse(result);
        assertTrue(outContent.toString().contains("Username already exists!"));
    }

    @Test
    void testCreateAdminAccount_Success() {
        boolean result = service.createAdminAccount("newadmin", "adminpass");
        assertTrue(result);
        assertTrue(outContent.toString().contains("Admin registered successfully!"));
    }

    @Test
    void testCreateAdminAccount_DuplicateUsername() {
        service.createAdminAccount("duplicate", "pass1");
        outContent.reset();
        
        boolean result = service.createAdminAccount("duplicate", "pass2");
        assertFalse(result);
        assertTrue(outContent.toString().contains("Username already exists!"));
    }

    @Test
    void testAuthenticateUser_Success() {
        service.createCustomerAccount("authuser", "authpass");
        outContent.reset();
        
        CustomerAccount result = service.authenticateUser("authuser", "authpass");
        assertNotNull(result);
        assertEquals("authuser", result.getAccountName());
        assertEquals("CUSTOMER", result.getAccountRole());
        assertTrue(outContent.toString().contains("Login successful!"));
    }

    @Test
    void testAuthenticateUser_WrongPassword() {
        service.createCustomerAccount("authuser", "correctpass");
        outContent.reset();
        
        CustomerAccount result = service.authenticateUser("authuser", "wrongpass");
        assertNull(result);
        assertTrue(outContent.toString().contains("Invalid username or password!"));
    }

    @Test
    void testAuthenticateUser_NonExistentUser() {
        CustomerAccount result = service.authenticateUser("nonexistent", "password");
        assertNull(result);
        assertTrue(outContent.toString().contains("Invalid username or password!"));
    }

    @Test
    void testAuthenticateUser_AdminUser() {
        service.createAdminAccount("adminuser", "adminpass");
        outContent.reset();
        
        CustomerAccount result = service.authenticateUser("adminuser", "adminpass");
        assertNotNull(result);
        assertEquals("ADMIN", result.getAccountRole());
        assertTrue(result.hasAdminPrivileges());
    }

    @Test
    void testLocateById_Success() {
        service.createCustomerAccount("locateuser", "locatepass");
        CustomerAccount created = service.authenticateUser("locateuser", "locatepass");
        
        CustomerAccount result = service.locateById(created.getAccountId());
        assertNotNull(result);
        assertEquals(created.getAccountId(), result.getAccountId());
        assertEquals("locateuser", result.getAccountName());
    }

    @Test
    void testLocateById_NonExistent() {
        CustomerAccount result = service.locateById(99999);
        assertNull(result);
    }
}

