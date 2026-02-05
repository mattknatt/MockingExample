package com.example.shop;

public class ItemDiscount implements Discount {
    @Override
    public double apply(double price, double discountPercentage) {
        return price - (price * discountPercentage);
    }
}
