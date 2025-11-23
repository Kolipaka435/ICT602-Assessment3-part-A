package org.example.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CustomerAccountTest {
    private CustomerAccount account;

    @BeforeEach
    void setUp() {
        account = new CustomerAccount();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(account);
        assertEquals(0, account.getAccountId());
        assertNull(account.getAccountName());
        assertNull(account.getAccountPassword());
        assertNull(account.getAccountRole());
    }

    @Test
    void testConstructorWithNamePasswordRole() {
        CustomerAccount newAccount = new CustomerAccount("testuser", "password123", "CUSTOMER");
        assertEquals("testuser", newAccount.getAccountName());
        assertEquals("password123", newAccount.getAccountPassword());
        assertEquals("CUSTOMER", newAccount.getAccountRole());
        assertEquals(0, newAccount.getAccountId());
    }

    @Test
    void testFullConstructor() {
        CustomerAccount newAccount = new CustomerAccount(1, "testuser", "password123", "ADMIN");
        assertEquals(1, newAccount.getAccountId());
        assertEquals("testuser", newAccount.getAccountName());
        assertEquals("password123", newAccount.getAccountPassword());
        assertEquals("ADMIN", newAccount.getAccountRole());
    }

    @Test
    void testSettersAndGetters() {
        account.setAccountId(5);
        account.setAccountName("john");
        account.setAccountPassword("secret");
        account.setAccountRole("CUSTOMER");

        assertEquals(5, account.getAccountId());
        assertEquals("john", account.getAccountName());
        assertEquals("secret", account.getAccountPassword());
        assertEquals("CUSTOMER", account.getAccountRole());
    }

    @Test
    void testHasAdminPrivileges_AdminRole() {
        account.setAccountRole("ADMIN");
        assertTrue(account.hasAdminPrivileges());
    }

    @Test
    void testHasAdminPrivileges_CustomerRole() {
        account.setAccountRole("CUSTOMER");
        assertFalse(account.hasAdminPrivileges());
    }

    @Test
    void testHasAdminPrivileges_NullRole() {
        account.setAccountRole(null);
        assertFalse(account.hasAdminPrivileges());
    }

    @Test
    void testIsRegularCustomer_CustomerRole() {
        account.setAccountRole("CUSTOMER");
        assertTrue(account.isRegularCustomer());
    }

    @Test
    void testIsRegularCustomer_AdminRole() {
        account.setAccountRole("ADMIN");
        assertFalse(account.isRegularCustomer());
    }

    @Test
    void testIsRegularCustomer_NullRole() {
        account.setAccountRole(null);
        assertFalse(account.isRegularCustomer());
    }
}

