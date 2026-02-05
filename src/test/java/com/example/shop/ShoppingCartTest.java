package com.example.shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ShoppingCartTest {

    private ShoppingCart cart;
    private Item item1;
    private Item item2;
    private Item item3;


    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
        item1 = new Item(10, 2);
        item2 = new Item(150, 4);
        item3 = new Item(100, 3);
        cart.addItem(item1);
        cart.addItem(item2);
        cart.addItem(item3);
    }

    @Test
    void addItem_shouldSaveItemToCart() {
        Item newItem = new Item();

        cart.addItem(newItem);

        assertThat(cart.getItems()).contains(newItem);
    }

    @Test
    void addItem_shouldThrowNPE_ifItemIsNull() {

        assertThatThrownBy(() -> cart.addItem(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Item cannot be null");

    }

    @Test
    void addItem_shouldIncreaseQuantity_ifItemAlreadyInCart() {
        ShoppingCart newCart = new ShoppingCart();
        Item newItem = new Item(159, 1);
        newCart.addItem(newItem);

        newCart.addItem(newItem);

        assertThat(newCart.getItems()).contains(newItem);
        assertThat(newCart.getItems()).hasSize(1);
        assertThat(newCart.getItems().getFirst().getQuantity()).isEqualTo(2);
    }

    @Test
    void removeItem_shouldRemoveItemFromCart() {
        cart.removeItem(item1);

        assertThat(cart.getItems()).doesNotContain(item1);
    }

    @Test
    void removeItem_doesNothing_ifItemNotInCart() {
        List<Item> beforeChange = new ArrayList<>(cart.getItems());
        Item newItem = new Item(100, 1);

        cart.removeItem(newItem);

        assertThat(cart.getItems()).hasSameSizeAs(beforeChange).containsExactlyElementsOf(beforeChange);
    }

    @Test
    void removeItem_shouldThrowNPE_ifItemIsNull() {
        assertThatThrownBy(() -> cart.removeItem(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Item cannot be null");
    }

    @Test
    void calculateTotalPrice_shouldReturnPriceOfAllAddedItems() {

        double totalPrice = cart.calculateTotalPrice(cart.getItems());

        assertThat(totalPrice).isEqualTo(
                item1.getPrice() * item1.getQuantity()
                        + item2.getPrice() * item2.getQuantity()
                        + item3.getPrice() * item3.getQuantity());

    }

    @Test
    void calculatePrice_shouldReturnZero_ifCartIsEmpty() {
        List<Item> emptyCart = new ArrayList<>();

        assertThat(cart.calculateTotalPrice(emptyCart)).isZero();
    }

    @Test
    void applyItemDiscount_returnsDiscountedPrice() {
        Discount discount = new DiscountCalculator();
        double discountPercentage = 0.01;

        double newPrice = discount.apply(item1.getPrice(), discountPercentage);

        assertThat(newPrice).isEqualTo(item1.getPrice() - (item1.getPrice()) * discountPercentage);
    }

    @Test
    void applyCartDiscount_returnsDiscountedPrice_ofItemsInCart() {
        Discount discount = new DiscountCalculator();
        double totalPrice = cart.calculateTotalPrice(cart.getItems());
        double discountPercentage = 0.01;

        double discountedCartPrice = discount.apply(totalPrice, discountPercentage);

        assertThat(discountedCartPrice).isEqualTo(totalPrice - (totalPrice * discountPercentage));
    }

    @Test
    void updateQuantity_shouldIncreaseTotalPriceInCart_whenIncreasedQuantity() {
        List<Item> cartItems = cart.getItems();
        Item item = cartItems.getFirst();
        int quantity = 2;
        double originalPrice = cart.calculateTotalPrice(cartItems);

        double expectedIncrease = item.getPrice() * quantity;
        cart.updateQuantity(item, item.getQuantity() + quantity);
        double updatedPrice = cart.calculateTotalPrice(cartItems);


        assertThat(updatedPrice).isEqualTo(originalPrice + expectedIncrease);

    }

    @Test
    void updateQuantity_shouldDecreaseTotalPriceInCart_whenDecreasedQuantity() {
        List<Item> cartItems = cart.getItems();
        Item item = cartItems.getFirst();
        int quantity = 1;
        double originalPrice = cart.calculateTotalPrice(cartItems);

        double expectedDecrease = item.getPrice() * quantity;
        cart.updateQuantity(item, item.getQuantity() - quantity);
        double updatedPrice = cart.calculateTotalPrice(cartItems);


        assertThat(updatedPrice).isEqualTo(originalPrice - expectedDecrease);

    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void updateQuantity_shouldThrowException_whenQuantityZeroOrNegative(int quantity) {
        Item item = cart.getItems().getFirst();

        assertThatThrownBy(() -> cart.updateQuantity(item, quantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be greater than 0");

    }

    @Test
    void updateQuantity_shouldThrowNPE_ifItemIsNull() {

        assertThatThrownBy(() -> cart.updateQuantity(null, 2))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Item cannot be null");

    }
}
