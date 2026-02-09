package com.example.shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * Unit tests for the {@link ShoppingCart} class.
 * Tests adding, removing, updating items, and calculating total prices and discounts.
 */
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

    @Nested
    @DisplayName("Add Item Tests")
    class AddItemTests {
        /**
         * Tests that adding a new item successfully saves it to the cart.
         */
        @Test
        @DisplayName("Add item: should save item to cart")
        void addItem_shouldSaveItemToCart() {
            Item newItem = new Item();

            cart.addItem(newItem);

            assertThat(cart.getItems()).contains(newItem);
        }

        /**
         * Tests that adding a null item throws a NullPointerException.
         */
        @Test
        @DisplayName("Add item: should throw NPE if item is null")
        void addItem_shouldThrowNPE_ifItemIsNull() {

            assertThatThrownBy(() -> cart.addItem(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Item cannot be null");

        }

        /**
         * Tests that adding an item that is already in the cart increases its quantity.
         */
        @Test
        @DisplayName("Add item: should increase quantity if item already in cart")
        void addItem_shouldIncreaseQuantity_ifItemAlreadyInCart() {
            ShoppingCart newCart = new ShoppingCart();
            Item newItem = new Item(159, 1);
            newCart.addItem(newItem);

            newCart.addItem(newItem);

            assertThat(newCart.getItems()).contains(newItem);
            assertThat(newCart.getItems()).hasSize(1);
            assertThat(newCart.getItems().getFirst().getQuantity()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Remove Item Tests")
    class RemoveItemTests {
        /**
         * Tests that removing an item successfully removes it from the cart.
         */
        @Test
        @DisplayName("Remove item: should remove item from cart")
        void removeItem_shouldRemoveItemFromCart() {
            cart.removeItem(item1);

            assertThat(cart.getItems()).doesNotContain(item1);
        }

        /**
         * Tests that trying to remove an item not in the cart does nothing.
         */
        @Test
        @DisplayName("Remove item: does nothing if item not in cart")
        void removeItem_doesNothing_ifItemNotInCart() {
            List<Item> beforeChange = new ArrayList<>(cart.getItems());
            Item newItem = new Item(100, 1);

            cart.removeItem(newItem);

            assertThat(cart.getItems()).hasSameSizeAs(beforeChange).containsExactlyElementsOf(beforeChange);
        }

        /**
         * Tests that trying to remove a null item throws a NullPointerException.
         */
        @Test
        @DisplayName("Remove item: should throw NPE if item is null")
        void removeItem_shouldThrowNPE_ifItemIsNull() {
            assertThatThrownBy(() -> cart.removeItem(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Item cannot be null");
        }
    }

    @Nested
    @DisplayName("Calculate Price Tests")
    class CalculatePriceTests {
        /**
         * Tests that the total price is correctly calculated for all items in the cart.
         */
        @Test
        @DisplayName("Calculate total price: should return price of all added items")
        void calculateTotalPrice_shouldReturnPriceOfAllAddedItems() {

            double totalPrice = cart.calculateTotalPrice(cart.getItems());

            assertThat(totalPrice).isEqualTo(
                    item1.getPrice() * item1.getQuantity()
                            + item2.getPrice() * item2.getQuantity()
                            + item3.getPrice() * item3.getQuantity());

        }

        /**
         * Tests that the total price is zero when the cart is empty.
         */
        @Test
        @DisplayName("Calculate total price: should return zero if cart is empty")
        void calculatePrice_shouldReturnZero_ifCartIsEmpty() {
            List<Item> emptyCart = new ArrayList<>();

            assertThat(cart.calculateTotalPrice(emptyCart)).isZero();
        }

        /**
         * Tests that a discount is correctly applied to an individual item price.
         */
        @Test
        @DisplayName("Discount: should return discounted price for item")
        void applyItemDiscount_returnsDiscountedPrice() {
            Discount discount = new DiscountCalculator();
            double discountPercentage = 0.01;

            double newPrice = discount.apply(item1.getPrice(), discountPercentage);

            assertThat(newPrice).isEqualTo(item1.getPrice() - (item1.getPrice()) * discountPercentage);
        }

        /**
         * Tests that a discount is correctly applied to the total cart price.
         */
        @Test
        @DisplayName("Discount: should return discounted price for whole cart")
        void applyCartDiscount_returnsDiscountedPrice_ofItemsInCart() {
            Discount discount = new DiscountCalculator();
            double totalPrice = cart.calculateTotalPrice(cart.getItems());
            double discountPercentage = 0.01;

            double discountedCartPrice = discount.apply(totalPrice, discountPercentage);

            assertThat(discountedCartPrice).isEqualTo(totalPrice - (totalPrice * discountPercentage));
        }
    }

    @Nested
    @DisplayName("Update Quantity Tests")
    class UpdateQuantityTests {
        /**
         * Tests that increasing the quantity of an item in the cart correctly increases the total price.
         */
        @Test
        @DisplayName("Update quantity: should increase total price when quantity is increased")
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

        /**
         * Tests that decreasing the quantity of an item in the cart correctly decreases the total price.
         */
        @Test
        @DisplayName("Update quantity: should decrease total price when quantity is decreased")
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

        /**
         * Tests that updating an item with a zero or negative quantity throws an IllegalArgumentException.
         *
         * @param quantity the invalid quantity to test
         */
        @ParameterizedTest
        @ValueSource(ints = {0, -1, -100})
        @DisplayName("Update quantity: should throw exception when quantity is zero or negative")
        void updateQuantity_shouldThrowException_whenQuantityZeroOrNegative(int quantity) {
            Item item = cart.getItems().getFirst();

            assertThatThrownBy(() -> cart.updateQuantity(item, quantity))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Quantity must be greater than 0");

        }

        /**
         * Tests that updating the quantity of a null item throws a NullPointerException.
         */
        @Test
        @DisplayName("Update quantity: should throw NPE if item is null")
        void updateQuantity_shouldThrowNPE_ifItemIsNull() {

            assertThatThrownBy(() -> cart.updateQuantity(null, 2))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Item cannot be null");
        }
    }
}
