package com.example.shop;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class ShoppingCartTest {
    
    @Test
    void addItem_shouldSaveItemToCart() {
        ShoppingCart cart = new ShoppingCart();
        Item item = new Item();

        cart.addItem(item);

        assertThat(cart.getItems()).contains(item);

    }

    @Test
    void removeItem_shouldRemoveItemFromCart() {
        ShoppingCart cart = new ShoppingCart();
        Item item = new Item();
        cart.addItem(item);

        cart.removeItem(item);

        assertThat(cart.getItems()).doesNotContain(item);

    }
    
    @Test
    void calculateTotalPrice_shouldReturnPriceOfAllAddedItems() {
        ShoppingCart cart = new ShoppingCart();
        Item item1 = new Item(15.0);
        Item item2 = new Item(100.0);
        Item item3 = new Item(210.95);
        cart.addItem(item1);
        cart.addItem(item2);
        cart.addItem(item3);

        double totalPrice = cart.calculateTotalPrice(cart.getItems());

        assertThat(totalPrice).isEqualTo(item1.getPrice() +  item2.getPrice() + item3.getPrice());

    }
    
    @Test
    void applyItemDiscount_returnsDiscountedPrice() {
        Discount discount = new ItemDiscount();
        double discountPercentage = 0.01;
        Item item = new Item(15.0);

        double newPrice = discount.apply(item.getPrice(),  discountPercentage);

        assertThat(newPrice).isEqualTo(item.getPrice() - (item.getPrice()) * discountPercentage);
        
    }

}