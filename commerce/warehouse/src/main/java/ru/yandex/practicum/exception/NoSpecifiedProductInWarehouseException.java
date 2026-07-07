package ru.yandex.practicum.exception;

public class NoSpecifiedProductInWarehouseException extends RuntimeException { // товар не указан на складе
    public NoSpecifiedProductInWarehouseException(String message) {
        super(message);
    }
}
