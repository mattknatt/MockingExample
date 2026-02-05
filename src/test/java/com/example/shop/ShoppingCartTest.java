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

}