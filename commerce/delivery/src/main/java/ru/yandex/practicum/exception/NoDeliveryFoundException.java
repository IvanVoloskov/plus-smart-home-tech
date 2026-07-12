package ru.yandex.practicum.exception;

public class NoDeliveryFoundException extends RuntimeException { // Исключение «Доставка не найдена»
    public NoDeliveryFoundException(String message) {
        super(message);
    }
}
