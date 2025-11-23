package org.example.model;

// This class represents an item in a customer's shopping cart
// It combines a product with a quantity, allowing customers to add multiple units of the same product
public class ShoppingCartEntry {
    // The product that the customer wants to purchase
    private InventoryItem inventoryItem;
    // The number of units of this product that the customer wants to buy
    private int entryQuantity;

    // Constructor for creating a cart item with a product and desired quantity
    // Used when customers add products to their shopping cart
    public ShoppingCartEntry(InventoryItem inventoryItem, int entryQuantity) {
        this.inventoryItem = inventoryItem;
        this.entryQuantity = entryQuantity;
    }

    public InventoryItem getInventoryItem() {
        return inventoryItem;
    }

    public void setInventoryItem(InventoryItem inventoryItem) {
        this.inventoryItem = inventoryItem;
    }

    public int getEntryQuantity() {
        return entryQuantity;
    }

    public void setEntryQuantity(int entryQuantity) {
        this.entryQuantity = entryQuantity;
    }

    // Calculates the subtotal for this cart item by multiplying the product price by the quantity
    // This represents the total cost for all units of this specific product in the cart
    public double calculateSubtotal() {
        return inventoryItem.getItemPrice() * entryQuantity;
    }
}

