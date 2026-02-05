package com.example.shop;


import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    public List<Item> items = new ArrayList<>();

    public void addItem(Item item) {
        items.add(item);
    }

    public List<Item> getItems() {
        return items;
    }
}
