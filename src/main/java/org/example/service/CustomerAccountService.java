package org.example.service;

import org.example.dao.CustomerAccountDAO;
import org.example.model.CustomerAccount;

// This service class provides business logic for user-related operations
// It acts as an intermediary between the presentation layer and the data access layer
public class CustomerAccountService {
    // Data Access Object for performing database operations on users
    private final CustomerAccountDAO accountDAO;

    // Constructor that initializes the CustomerAccountDAO for database operations
    public CustomerAccountService() {
        this.accountDAO = new CustomerAccountDAO();
    }

    // Registers a new customer account in the system
    // Validates that the username is not already taken before creating the account
    // Returns true if registration was successful, false if username exists or registration failed
    public boolean createCustomerAccount(String accountName, String accountPassword) {
        if (accountDAO.checkUsernameAvailability(accountName)) {
            System.out.println("Username already exists!"); // Inform user that username is taken
            return false;
        }
        // Create a new user object with CUSTOMER role
        CustomerAccount account = new CustomerAccount(accountName, accountPassword, "CUSTOMER");
        boolean success = accountDAO.insertRecord(account); // Attempt to save the user to the database
        if (success) {
            System.out.println("Customer registered successfully!"); // Confirm successful registration
        } else {
            System.out.println("Registration failed!"); // Inform user of registration failure
        }
        return success;
    }

    // Registers a new administrator account in the system
    // Validates that the username is not already taken before creating the account
    // Returns true if registration was successful, false if username exists or registration failed
    public boolean createAdminAccount(String accountName, String accountPassword) {
        if (accountDAO.checkUsernameAvailability(accountName)) {
            System.out.println("Username already exists!"); // Inform user that username is taken
            return false;
        }
        // Create a new user object with ADMIN role
        CustomerAccount account = new CustomerAccount(accountName, accountPassword, "ADMIN");
        boolean success = accountDAO.insertRecord(account); // Attempt to save the user to the database
        if (success) {
            System.out.println("Admin registered successfully!"); // Confirm successful registration
        } else {
            System.out.println("Registration failed!"); // Inform user of registration failure
        }
        return success;
    }

    // Authenticates a user by verifying their username and password
    // Returns the CustomerAccount object if authentication succeeds, null if credentials are invalid
    public CustomerAccount authenticateUser(String accountName, String accountPassword) {
        CustomerAccount account = accountDAO.authenticateUser(accountName, accountPassword); // Attempt to authenticate the user
        if (account != null) {
            // If login is successful, display a welcome message with username and role
            System.out.println("Login successful! Welcome, " + account.getAccountName() + " (" + account.getAccountRole() + ")");
        } else {
            System.out.println("Invalid username or password!"); // Inform user of authentication failure
        }
        return account; // Return the user object or null
    }

    // Retrieves a user from the database by their unique ID
    // Returns the CustomerAccount object if found, null if the user does not exist
    public CustomerAccount locateById(int accountId) {
        return accountDAO.locateById(accountId); // Delegate to the DAO to retrieve the user
    }
}
