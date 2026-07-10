package ru.yandex.practicum.exception;

public class ProductInShoppingCartNotInWarehouse extends RuntimeException { // Товар в корзине, но отсутствует на складе
    public ProductInShoppingCartNotInWarehouse(String message) {
        super(message);
    }
}
