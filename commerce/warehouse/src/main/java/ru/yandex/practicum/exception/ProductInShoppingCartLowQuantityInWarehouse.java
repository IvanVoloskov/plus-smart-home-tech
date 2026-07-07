package ru.yandex.practicum.exception;

public class ProductInShoppingCartLowQuantityInWarehouse extends RuntimeException { // низкий остаток на складе
    public ProductInShoppingCartLowQuantityInWarehouse(String message) {
        super(message);
    }
}
