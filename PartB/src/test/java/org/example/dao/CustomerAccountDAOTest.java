package org.example.dao;

import org.example.model.CustomerAccount;
import org.example.util.TestDatabaseHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CustomerAccountDAOTest {
    private CustomerAccountDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        TestDatabaseHelper.setupTestDatabase();
        dao = new CustomerAccountDAO();
    }

    @AfterEach
    void tearDown() throws Exception {
        TestDatabaseHelper.cleanupTestDatabase();
        TestDatabaseHelper.restoreOriginalDatabase();
    }

    @Test
    void testInsertRecord_Success() {
        CustomerAccount account = new CustomerAccount("testuser", "password123", "CUSTOMER");
        boolean result = dao.insertRecord(account);
        assertTrue(result);
    }

    @Test
    void testInsertRecord_DuplicateUsername() {
        CustomerAccount account1 = new CustomerAccount("duplicate", "pass1", "CUSTOMER");
        dao.insertRecord(account1);
        
        CustomerAccount account2 = new CustomerAccount("duplicate", "pass2", "CUSTOMER");
        boolean result = dao.insertRecord(account2);
        assertFalse(result);
    }

    @Test
    void testInsertRecord_AdminRole() {
        CustomerAccount account = new CustomerAccount("admin", "admin123", "ADMIN");
        boolean result = dao.insertRecord(account);
        assertTrue(result);
    }

    @Test
    void testAuthenticateUser_Success() {
        CustomerAccount account = new CustomerAccount("authuser", "authpass", "CUSTOMER");
        dao.insertRecord(account);
        
        CustomerAccount result = dao.authenticateUser("authuser", "authpass");
        assertNotNull(result);
        assertEquals("authuser", result.getAccountName());
        assertEquals("authpass", result.getAccountPassword());
        assertEquals("CUSTOMER", result.getAccountRole());
    }

    @Test
    void testAuthenticateUser_WrongPassword() {
        CustomerAccount account = new CustomerAccount("authuser", "correctpass", "CUSTOMER");
        dao.insertRecord(account);
        
        CustomerAccount result = dao.authenticateUser("authuser", "wrongpass");
        assertNull(result);
    }

    @Test
    void testAuthenticateUser_NonExistentUser() {
        CustomerAccount result = dao.authenticateUser("nonexistent", "password");
        assertNull(result);
    }

    @Test
    void testLocateById_Success() {
        CustomerAccount account = new CustomerAccount("locateuser", "locatepass", "CUSTOMER");
        dao.insertRecord(account);
        
        CustomerAccount found = dao.authenticateUser("locateuser", "locatepass");
        assertNotNull(found);
        
        CustomerAccount result = dao.locateById(found.getAccountId());
        assertNotNull(result);
        assertEquals(found.getAccountId(), result.getAccountId());
        assertEquals("locateuser", result.getAccountName());
    }

    @Test
    void testLocateById_NonExistent() {
        CustomerAccount result = dao.locateById(99999);
        assertNull(result);
    }

    @Test
    void testCheckUsernameAvailability_Available() {
        boolean result = dao.checkUsernameAvailability("availableuser");
        assertFalse(result);
    }

    @Test
    void testCheckUsernameAvailability_Taken() {
        CustomerAccount account = new CustomerAccount("takenuser", "pass", "CUSTOMER");
        dao.insertRecord(account);
        
        boolean result = dao.checkUsernameAvailability("takenuser");
        assertTrue(result);
    }

    @Test
    void testCheckUsernameAvailability_CaseSensitive() {
        CustomerAccount account = new CustomerAccount("CaseUser", "pass", "CUSTOMER");
        dao.insertRecord(account);
        
        boolean result = dao.checkUsernameAvailability("caseuser");
        assertFalse(result);
    }
}

