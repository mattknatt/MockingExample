package com.example.shop;


import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    public List<Item> items = new ArrayList<>();

    public void addItem(Item item) {
        if(item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }

        if (!items.contains(item)) {
            items.add(item);
        } else {
            updateQuantity(item, item.getQuantity() + 1);
        }
    }

    public void removeItem(Item item) {
        if(item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }

        if (!items.contains(item)) {
            return;
        }
        items.remove(item);
    }

    public List<Item> getItems() {
        return items;
    }

    public double calculateTotalPrice(List<Item> items) {
        double totalPrice = 0;
        for( Item item : items) {

            totalPrice += (item.getPrice() *  item.getQuantity());
        }
        return totalPrice;
    }

    public void updateQuantity(Item item, int quantity) {
        if(item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be greater than 0");

        item.setQuantity(quantity);
    }
}
