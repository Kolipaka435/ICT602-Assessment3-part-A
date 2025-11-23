package org.example.service;

import org.example.dao.*;
import org.example.model.*;

import java.util.List;

// This service class provides business logic for order-related operations
// It coordinates between multiple DAOs to handle the complete order lifecycle from placement to delivery
public class PurchaseTransactionService {
    // Data Access Objects for performing database operations on orders, order items, payments, and products
    private final PurchaseTransactionDAO transactionDAO;
    private final TransactionLineItemDAO lineItemDAO;
    private final PaymentRecordDAO paymentRecordDAO;
    private final InventoryItemDAO inventoryDAO;

    // Constructor that initializes all the DAOs needed for order processing
    public PurchaseTransactionService() {
        this.transactionDAO = new PurchaseTransactionDAO();
        this.lineItemDAO = new TransactionLineItemDAO();
        this.paymentRecordDAO = new PaymentRecordDAO();
        this.inventoryDAO = new InventoryItemDAO();
    }

    // Places a new order by creating order records, order items, and payment records in the database
    // Calculates the total from cart items and creates all necessary database entries
    // Returns the order ID if successful, -1 if order creation failed
    public int createPurchaseTransaction(int accountId, List<ShoppingCartEntry> cartEntries, String paymentType) {
        // Calculate the total amount by summing up all cart item subtotals
        double total = cartEntries.stream()
            .mapToDouble(ShoppingCartEntry::calculateSubtotal)
            .sum();

        // Create a new order with CREATED status, which awaits admin approval
        PurchaseTransaction transaction = new PurchaseTransaction(accountId, "CREATED", total);
        int transactionId = transactionDAO.insertTransaction(transaction); // Save the order to the database and get the generated ID

        if (transactionId > 0) {
            // Add each cart item as an order item in the database
            for (ShoppingCartEntry entry : cartEntries) {
                TransactionLineItem lineItem = new TransactionLineItem(
                    transactionId,
                    entry.getInventoryItem().getItemId(),
                    entry.getEntryQuantity(),
                    entry.getInventoryItem().getItemPrice() // Store the price at time of order for historical record
                );
                lineItemDAO.insertRecord(lineItem); // Save the order item to the database
            }

            // Create a payment record for this order (simulated - always success for now)
            PaymentRecord paymentRecord = new PaymentRecord(transactionId, paymentType, "SUCCESS", total);
            paymentRecordDAO.insertRecord(paymentRecord); // Save the payment to the database

            // Display order confirmation information to the user
            System.out.println("Order placed successfully! Order ID: " + transactionId);
            System.out.println("Payment method: " + paymentType);
            System.out.println("Total amount: $" + String.format("%.2f", total));
            return transactionId;
        }
        return -1; // Return -1 if order creation failed
    }

    // Accepts a pending order, which involves checking stock availability and deducting inventory
    // Only orders with CREATED status can be accepted
    // Returns true if the order was successfully accepted, false if validation fails or an error occurs
    public boolean approvePurchaseTransaction(int transactionId) {
        PurchaseTransaction transaction = transactionDAO.locateById(transactionId); // Retrieve the order from the database
        if (transaction == null) {
            System.out.println("Order not found!"); // Inform admin if order doesn't exist
            return false;
        }

        // Validate that the order is in CREATED status and can be accepted
        if (!"CREATED".equals(transaction.getTransactionStatus())) {
            System.out.println("Order cannot be accepted. Current status: " + transaction.getTransactionStatus());
            return false;
        }

        // Get all items in the order and verify stock availability before deducting inventory
        List<TransactionLineItem> lineItems = lineItemDAO.locateByTransactionId(transactionId);
        for (TransactionLineItem lineItem : lineItems) {
            InventoryItem item = inventoryDAO.locateById(lineItem.getInventoryItemId()); // Get the product information
            // Check if there is sufficient stock available for this order item
            if (item.getItemStock() < lineItem.getItemQuantity()) {
                System.out.println("Insufficient stock for product ID: " + lineItem.getInventoryItemId());
                return false; // Cannot accept order if stock is insufficient
            }
            // Deduct the ordered quantity from the product's stock
            inventoryDAO.adjustStockLevel(lineItem.getInventoryItemId(), lineItem.getItemQuantity());
        }

        // Update the order status to ACCEPTED after successfully processing inventory
        boolean success = transactionDAO.modifyStatus(transactionId, "ACCEPTED");
        if (success) {
            System.out.println("Order #" + transactionId + " has been ACCEPTED!");
            System.out.println("Inventory deducted. Customer will be notified.");
        }
        return success;
    }

    // Rejects a pending order, which involves updating the order status and refunding the payment
    // Only orders with CREATED status can be rejected
    // Returns true if the order was successfully rejected, false if validation fails or an error occurs
    public boolean declinePurchaseTransaction(int transactionId) {
        PurchaseTransaction transaction = transactionDAO.locateById(transactionId); // Retrieve the order from the database
        if (transaction == null) {
            System.out.println("Order not found!"); // Inform admin if order doesn't exist
            return false;
        }

        // Validate that the order is in CREATED status and can be rejected
        if (!"CREATED".equals(transaction.getTransactionStatus())) {
            System.out.println("Order cannot be rejected. Current status: " + transaction.getTransactionStatus());
            return false;
        }

        // Update the order status to REJECTED
        boolean success = transactionDAO.modifyStatus(transactionId, "REJECTED");
        if (success) {
            // Update the payment status to REFUNDED (simulated refund process)
            paymentRecordDAO.modifyPaymentStatus(transactionId, "REFUNDED");
            System.out.println("Order #" + transactionId + " has been REJECTED!");
            System.out.println("Payment refunded (simulated). Customer will be notified.");
        }
        return success;
    }

    // Marks an accepted order as delivered, completing the order fulfillment process
    // Only orders with ACCEPTED status can be marked as delivered
    // Returns true if the order was successfully marked as delivered, false if validation fails or an error occurs
    public boolean markAsDelivered(int transactionId) {
        PurchaseTransaction transaction = transactionDAO.locateById(transactionId); // Retrieve the order from the database
        if (transaction == null) {
            System.out.println("Order not found!"); // Inform admin if order doesn't exist
            return false;
        }

        // Validate that the order is in ACCEPTED status and can be marked as delivered
        if (!"ACCEPTED".equals(transaction.getTransactionStatus())) {
            System.out.println("Only ACCEPTED orders can be marked as DELIVERED. Current status: " + transaction.getTransactionStatus());
            return false;
        }

        // Update the order status to DELIVERED
        boolean success = transactionDAO.modifyStatus(transactionId, "DELIVERED");
        if (success) {
            System.out.println("Order #" + transactionId + " has been marked as DELIVERED!");
            System.out.println("Customer will be notified.");
        }
        return success;
    }

    // Retrieves all orders from the database for admin review
    // Returns a list of all PurchaseTransaction objects sorted by order date (most recent first)
    public List<PurchaseTransaction> retrieveAllPurchaseTransactions() {
        return transactionDAO.retrieveAllRecords(); // Delegate to the DAO to retrieve all orders
    }

    // Retrieves all orders placed by a specific user, identified by their user ID
    // Returns a list of PurchaseTransaction objects for that user, sorted by order date (most recent first)
    public List<PurchaseTransaction> fetchUserPurchaseTransactions(int accountId) {
        return transactionDAO.locateByUserId(accountId); // Delegate to the DAO to retrieve user orders
    }

    // Retrieves a single order from the database by its unique ID
    // Returns the PurchaseTransaction object if found, null if the order does not exist
    public PurchaseTransaction fetchPurchaseTransactionById(int transactionId) {
        return transactionDAO.locateById(transactionId); // Delegate to the DAO to retrieve the order
    }
}
