package org.example;
import org.example.model.*;
import org.example.service.*;
import org.example.util.DataConnectionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// This is the main entry point for the Fashion E-Retail System (FERS) application
// It provides a console-based interface for both customers and administrators to interact with the system
public class Main {
    // Scanner object to read user input from the console throughout the application
    private static Scanner inputReader = new Scanner(System.in);
    // Service layer object that handles all user-related operations like registration and login
    private static CustomerAccountService accountService = new CustomerAccountService();
    // Service layer object that manages product operations such as adding, updating, and deleting products
    private static InventoryItemService inventoryService = new InventoryItemService();
    // Service layer object that handles order processing, acceptance, rejection, and delivery tracking
    private static PurchaseTransactionService transactionService = new PurchaseTransactionService();
    // Stores the currently logged-in user, null when no user is logged in
    private static CustomerAccount activeAccount = null;
    // Shopping cart that holds items the customer wants to purchase before checkout
    private static List<ShoppingCartEntry> shoppingCart = new ArrayList<>();

    // Main method that starts the application and runs the main program loop
    // It initializes the database and continuously displays the appropriate menu based on user login status
    public static void main(String[] args) {
        System.out.println("=== Fashion E-Retail System (FERS) ===");
        // Initialize the database by creating all necessary tables if they don't exist
        DataConnectionHelper.initializeDatabase();

        // Main application loop that runs continuously until the user exits
        // The menu displayed depends on whether a user is logged in and their role
        while (true) {
            if (activeAccount == null) {
                // Show the main menu for unauthenticated users (login/register options)
                showMainMenu();
            } else if (activeAccount.hasAdminPrivileges()) {
                // Show the admin menu with product management and order approval options
                showAdminMenu();
            } else {
                // Show the customer menu with shopping and order viewing options
                showCustomerMenu();
            }
        }
    }

    // Displays the main menu for users who are not logged in
    // Provides options to register as a new customer, login with existing credentials, or exit the application
    private static void showMainMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Register as Customer");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");

        // Read the user's menu choice from the console
        int choice = inputReader.nextInt();
        inputReader.nextLine(); // Consume the newline character left in the buffer

        // Process the user's choice and call the appropriate method
        switch (choice) {
            case 1 -> registerCustomer(); // Allow new users to create a customer account
            case 2 -> login(); // Allow existing users to authenticate and access the system
            case 3 -> {
                // Exit the application gracefully with a thank you message
                System.out.println("Thank you for using FERS!");
                System.exit(0);
            }
            default -> System.out.println("Invalid option!"); // Handle invalid menu selections
        }
    }

    // Handles the customer registration process by collecting username and password from the user
    // The user service will validate the input and create a new customer account in the database
    private static void registerCustomer() {
        System.out.print("Enter username: ");
        String accountName = inputReader.nextLine(); // Read the desired username from user input
        System.out.print("Enter password: ");
        String accountPassword = inputReader.nextLine(); // Read the desired password from user input
        // Attempt to register the new customer with the provided credentials
        accountService.createCustomerAccount(accountName, accountPassword);
    }

    // Handles user authentication by verifying username and password against the database
    // If login is successful, the current user is set and the shopping cart is initialized
    private static void login() {
        System.out.print("Enter username: ");
        String accountName = inputReader.nextLine(); // Read the username from user input
        System.out.print("Enter password: ");
        String accountPassword = inputReader.nextLine(); // Read the password from user input
        // Attempt to authenticate the user and retrieve their account information
        activeAccount = accountService.authenticateUser(accountName, accountPassword);
        if (activeAccount != null) {
            // Initialize a fresh shopping cart when a user successfully logs in
            shoppingCart = new ArrayList<>(); // Reset cart on login
        }
    }

    // Displays the administrative menu with options for managing products and processing orders
    // Only accessible to users with admin role, provides full system management capabilities
    private static void showAdminMenu() {
        System.out.println("\n--- Admin Menu ---");
        System.out.println("1. Add Product");
        System.out.println("2. Update Product");
        System.out.println("3. Delete Product");
        System.out.println("4. View All Products");
        System.out.println("5. View All Orders");
        System.out.println("6. Accept Order");
        System.out.println("7. Reject Order");
        System.out.println("8. Mark Order as Delivered");
        System.out.println("9. Logout");
        System.out.print("Choose an option: ");

        // Read the admin's menu choice from the console
        int choice = inputReader.nextInt();
        inputReader.nextLine(); // Consume the newline character left in the buffer

        // Process the admin's choice and execute the corresponding administrative function
        switch (choice) {
            case 1 -> addProduct(); // Add a new product to the inventory
            case 2 -> updateProduct(); // Modify existing product information
            case 3 -> deleteProduct(); // Remove a product from the inventory
            case 4 -> viewAllProducts(); // Display all products in the system
            case 5 -> viewAllOrders(); // View all orders placed by all customers
            case 6 -> acceptOrder(); // Approve a pending order and deduct inventory
            case 7 -> rejectOrder(); // Reject a pending order and refund payment
            case 8 -> markOrderDelivered(); // Mark an accepted order as delivered to the customer
            case 9 -> {
                // Log out the admin user and return to the main menu
                activeAccount = null;
                System.out.println("Logged out successfully!");
            }
            default -> System.out.println("Invalid option!"); // Handle invalid menu selections
        }
    }

    // Displays the customer menu with shopping and order management options
    // Provides all the functionality customers need to browse products, manage their cart, and place orders
    private static void showCustomerMenu() {
        System.out.println("\n--- Customer Menu ---");
        System.out.println("1. Browse Products");
        System.out.println("2. Add to Cart");
        System.out.println("3. View Cart");
        System.out.println("4. Remove from Cart");
        System.out.println("5. Checkout");
        System.out.println("6. View My Orders");
        System.out.println("7. Logout");
        System.out.print("Choose an option: ");

        // Read the customer's menu choice from the console
        int choice = inputReader.nextInt();
        inputReader.nextLine(); // Consume the newline character left in the buffer

        // Process the customer's choice and execute the corresponding shopping function
        switch (choice) {
            case 1 -> browseProducts(); // Display all available products for browsing
            case 2 -> addToCart(); // Add a product to the shopping cart with specified quantity
            case 3 -> viewCart(); // Display all items currently in the shopping cart with totals
            case 4 -> removeFromCart(); // Remove a specific product from the shopping cart
            case 5 -> checkout(); // Process the order, create payment, and submit for admin approval
            case 6 -> viewMyOrders(); // Display all orders placed by the current customer
            case 7 -> {
                // Log out the customer, clear the cart, and return to the main menu
                activeAccount = null;
                shoppingCart.clear(); // Clear all items from the cart when logging out
                System.out.println("Logged out successfully!");
            }
            default -> System.out.println("Invalid option!"); // Handle invalid menu selections
        }
    }

    // Allows the admin to add a new product to the inventory by collecting all product details
    // The product will be saved to the database and made available for customers to purchase
    private static void addProduct() {
        System.out.print("Enter product name: ");
        String itemName = inputReader.nextLine(); // Read the product name from admin input
        System.out.print("Enter description: ");
        String itemDescription = inputReader.nextLine(); // Read the product description from admin input
        System.out.print("Enter price: ");
        double itemPrice = inputReader.nextDouble(); // Read the product price from admin input
        System.out.print("Enter stock: ");
        int itemStock = inputReader.nextInt(); // Read the initial stock quantity from admin input
        inputReader.nextLine(); // Consume the newline character left in the buffer
        // Add the new product to the database through the product service
        inventoryService.insertInventoryItem(itemName, itemDescription, itemPrice, itemStock);
    }

    // Allows the admin to update an existing product's information by specifying the product ID
    // All product fields can be modified including name, description, price, and stock quantity
    private static void updateProduct() {
        System.out.print("Enter product ID to update: ");
        int itemId = inputReader.nextInt(); // Read the ID of the product to be updated
        inputReader.nextLine(); // Consume the newline character left in the buffer
        System.out.print("Enter new name: ");
        String itemName = inputReader.nextLine(); // Read the updated product name
        System.out.print("Enter new description: ");
        String itemDescription = inputReader.nextLine(); // Read the updated product description
        System.out.print("Enter new price: ");
        double itemPrice = inputReader.nextDouble(); // Read the updated product price
        System.out.print("Enter new stock: ");
        int itemStock = inputReader.nextInt(); // Read the updated stock quantity
        inputReader.nextLine(); // Consume the newline character left in the buffer
        // Update the product in the database with the new information
        inventoryService.modifyInventoryItem(itemId, itemName, itemDescription, itemPrice, itemStock);
    }

    // Allows the admin to delete a product from the inventory by specifying its ID
    // The product will be permanently removed from the database and will no longer be available for purchase
    private static void deleteProduct() {
        System.out.print("Enter product ID to delete: ");
        int itemId = inputReader.nextInt(); // Read the ID of the product to be deleted
        inputReader.nextLine(); // Consume the newline character left in the buffer
        // Delete the product from the database through the product service
        inventoryService.removeInventoryItem(itemId);
    }

    // Displays all products in the system in a formatted table for the admin to review
    // Shows product ID, name, price, and current stock level for inventory management
    private static void viewAllProducts() {
        // Retrieve all products from the database through the product service
        List<InventoryItem> items = inventoryService.retrieveAllInventoryItems();
        if (items.isEmpty()) {
            System.out.println("No products available."); // Inform admin if no products exist
        } else {
            System.out.println("\n--- All Products ---");
            // Print table header with formatted columns for better readability
            System.out.printf("%-5s %-30s %-10s %-10s%n", "ID", "Name", "Price", "Stock");
            // Iterate through all products and display their information in a formatted row
            for (InventoryItem item : items) {
                System.out.printf("%-5d %-30s $%-9.2f %-10d%n",
                    item.getItemId(), item.getItemName(), item.getItemPrice(), item.getItemStock());
            }
        }
    }

    // Displays all orders placed by all customers in a formatted table for admin review
    // Shows order details including ID, customer ID, status, date, and total amount for order management
    private static void viewAllOrders() {
        // Retrieve all orders from the database through the order service
        List<PurchaseTransaction> transactions = transactionService.retrieveAllPurchaseTransactions();
        if (transactions.isEmpty()) {
            System.out.println("No orders found."); // Inform admin if no orders exist
        } else {
            System.out.println("\n--- All Orders ---");
            // Print table header with formatted columns for better readability
            System.out.printf("%-8s %-10s %-15s %-20s %-10s%n",
                "Order ID", "User ID", "Status", "Order Date", "Total");
            // Iterate through all orders and display their information in a formatted row
            for (PurchaseTransaction transaction : transactions) {
                System.out.printf("%-8d %-10d %-15s %-20s $%-9.2f%n",
                    transaction.getTransactionId(), transaction.getCustomerAccountId(), transaction.getTransactionStatus(),
                    transaction.getTransactionDate().toString(), transaction.getTransactionTotal());
            }
        }
    }

    // Allows the admin to accept a pending order, which will deduct inventory and notify the customer
    // Only orders with CREATED status can be accepted, and stock availability is checked
    private static void acceptOrder() {
        System.out.print("Enter order ID to accept: ");
        int transactionId = inputReader.nextInt(); // Read the ID of the order to be accepted
        inputReader.nextLine(); // Consume the newline character left in the buffer
        // Process the order acceptance through the order service, which handles inventory deduction
        transactionService.approvePurchaseTransaction(transactionId);
    }

    // Allows the admin to reject a pending order, which will refund the payment and notify the customer
    // Only orders with CREATED status can be rejected, and the payment status will be updated to REFUNDED
    private static void rejectOrder() {
        System.out.print("Enter order ID to reject: ");
        int transactionId = inputReader.nextInt(); // Read the ID of the order to be rejected
        inputReader.nextLine(); // Consume the newline character left in the buffer
        // Process the order rejection through the order service, which handles payment refund
        transactionService.declinePurchaseTransaction(transactionId);
    }

    // Allows the admin to mark an accepted order as delivered, completing the order fulfillment process
    // Only orders with ACCEPTED status can be marked as delivered, indicating the product has reached the customer
    private static void markOrderDelivered() {
        System.out.print("Enter order ID to mark as delivered: ");
        int transactionId = inputReader.nextInt(); // Read the ID of the order to be marked as delivered
        inputReader.nextLine(); // Consume the newline character left in the buffer
        // Update the order status to DELIVERED through the order service
        transactionService.markAsDelivered(transactionId);
    }

    // Displays all available products to the customer in a formatted table for browsing
    // Shows product details including ID, name, description, price, and stock availability
    private static void browseProducts() {
        // Retrieve all products from the database through the product service
        List<InventoryItem> items = inventoryService.retrieveAllInventoryItems();
        if (items.isEmpty()) {
            System.out.println("No products available."); // Inform customer if no products exist
        } else {
            System.out.println("\n--- Available Products ---");
            // Print table header with formatted columns for better readability
            System.out.printf("%-5s %-30s %-50s %-10s %-10s%n",
                "ID", "Name", "Description", "Price", "Stock");
            // Iterate through all products and display their information in a formatted row
            for (InventoryItem item : items) {
                // Truncate long descriptions to 50 characters for better table formatting
                System.out.printf("%-5d %-30s %-50s $%-9.2f %-10d%n",
                    item.getItemId(), item.getItemName(),
                    item.getItemDescription().length() > 50 ? item.getItemDescription().substring(0, 47) + "..." : item.getItemDescription(),
                    item.getItemPrice(), item.getItemStock());
            }
        }
    }

    // Allows the customer to add a product to their shopping cart with a specified quantity
    // Validates product existence and stock availability before adding to cart
    private static void addToCart() {
        System.out.print("Enter product ID: ");
        int itemId = inputReader.nextInt(); // Read the ID of the product to add to cart
        System.out.print("Enter quantity: ");
        int entryQuantity = inputReader.nextInt(); // Read the desired quantity from customer input
        inputReader.nextLine(); // Consume the newline character left in the buffer

        // Retrieve the product from the database to verify it exists
        InventoryItem item = inventoryService.fetchInventoryItemById(itemId);
        if (item == null) {
            System.out.println("Product not found!"); // Inform customer if product doesn't exist
            return;
        }

        // Check if there is sufficient stock available for the requested quantity
        if (item.getItemStock() < entryQuantity) {
            System.out.println("Insufficient stock! Available: " + item.getItemStock()); // Inform customer of available stock
            return;
        }

        // Check if the product is already in the cart, and if so, update the quantity instead of adding duplicate
        boolean found = false;
        for (ShoppingCartEntry entry : shoppingCart) {
            if (entry.getInventoryItem().getItemId() == itemId) {
                // If product already exists in cart, add the new quantity to the existing quantity
                entry.setEntryQuantity(entry.getEntryQuantity() + entryQuantity);
                found = true;
                break;
            }
        }

        // If product is not already in cart, create a new cart item and add it
        if (!found) {
            shoppingCart.add(new ShoppingCartEntry(item, entryQuantity));
        }

        System.out.println("Product added to cart!"); // Confirm successful addition to cart
    }

    // Displays all items currently in the customer's shopping cart with detailed pricing information
    // Shows each item's details and calculates the total amount for the entire cart
    private static void viewCart() {
        if (shoppingCart.isEmpty()) {
            System.out.println("Your cart is empty."); // Inform customer if cart has no items
        } else {
            System.out.println("\n--- Your Cart ---");
            // Print table header with formatted columns for better readability
            System.out.printf("%-5s %-30s %-10s %-10s %-10s%n",
                "ID", "Name", "Price", "Quantity", "Subtotal");
            double total = 0; // Initialize total amount accumulator
            // Iterate through all cart items and display their information
            for (ShoppingCartEntry entry : shoppingCart) {
                double subtotal = entry.calculateSubtotal(); // Calculate subtotal for this item (price * quantity)
                total += subtotal; // Add item subtotal to the overall cart total
                // Display formatted row with product details and calculated subtotal
                System.out.printf("%-5d %-30s $%-9.2f %-10d $%-9.2f%n",
                    entry.getInventoryItem().getItemId(), entry.getInventoryItem().getItemName(),
                    entry.getInventoryItem().getItemPrice(), entry.getEntryQuantity(), subtotal);
            }
            // Display the final total amount for all items in the cart
            System.out.println("Total: $" + String.format("%.2f", total));
        }
    }

    // Allows the customer to remove a specific product from their shopping cart by product ID
    // The entire cart item is removed regardless of quantity, customer can re-add if needed
    private static void removeFromCart() {
        if (shoppingCart.isEmpty()) {
            System.out.println("Your cart is empty."); // Inform customer if cart has no items to remove
            return;
        }

        System.out.print("Enter product ID to remove: ");
        int itemId = inputReader.nextInt(); // Read the ID of the product to remove from cart
        inputReader.nextLine(); // Consume the newline character left in the buffer

        // Remove the cart item that matches the specified product ID
        shoppingCart.removeIf(entry -> entry.getInventoryItem().getItemId() == itemId);
        System.out.println("Product removed from cart."); // Confirm successful removal
    }

    // Processes the checkout by creating an order from the cart items and processing payment
    // The order is created with CREATED status and awaits admin approval before fulfillment
    private static void checkout() {
        if (shoppingCart.isEmpty()) {
            System.out.println("Your cart is empty!"); // Prevent checkout if cart has no items
            return;
        }

        viewCart(); // Display cart contents so customer can review before checkout
        System.out.println("\n--- Payment Method ---");
        System.out.println("1. Online");
        System.out.println("2. Card");
        System.out.println("3. COD (Cash on Delivery)");
        System.out.print("Choose payment method: ");

        // Read the customer's payment method choice from the console
        int choice = inputReader.nextInt();
        inputReader.nextLine(); // Consume the newline character left in the buffer

        // Convert the numeric choice to the corresponding payment method string
        String paymentType;
        switch (choice) {
            case 1 -> paymentType = "ONLINE"; // Online payment method
            case 2 -> paymentType = "CARD"; // Card payment method
            case 3 -> paymentType = "COD"; // Cash on delivery payment method
            default -> {
                System.out.println("Invalid payment method!"); // Handle invalid payment method selection
                return;
            }
        }

        // Place the order through the order service, which creates order, order items, and payment records
        int transactionId = transactionService.createPurchaseTransaction(activeAccount.getAccountId(), shoppingCart, paymentType);
        if (transactionId > 0) {
            shoppingCart.clear(); // Clear the cart after successful order placement
            System.out.println("Order placed successfully! Waiting for admin approval."); // Confirm order creation
        }
    }

    // Displays all orders placed by the currently logged-in customer in a formatted table
    // Shows order status, date, and total amount so customers can track their order history
    private static void viewMyOrders() {
        // Retrieve all orders for the current user from the database through the order service
        List<PurchaseTransaction> transactions = transactionService.fetchUserPurchaseTransactions(activeAccount.getAccountId());
        if (transactions.isEmpty()) {
            System.out.println("You have no orders yet."); // Inform customer if they have no order history
        } else {
            System.out.println("\n--- My Orders ---");
            // Print table header with formatted columns for better readability
            System.out.printf("%-8s %-15s %-20s %-10s%n",
                "Order ID", "Status", "Order Date", "Total");
            // Iterate through all customer orders and display their information in a formatted row
            for (PurchaseTransaction transaction : transactions) {
                System.out.printf("%-8d %-15s %-20s $%-9.2f%n",
                    transaction.getTransactionId(), transaction.getTransactionStatus(),
                    transaction.getTransactionDate().toString(), transaction.getTransactionTotal());
            }
        }
    }
}

