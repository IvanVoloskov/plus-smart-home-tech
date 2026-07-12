package ru.yandex.practicum.exception;

public class NoOrderFoundException extends RuntimeException { // Исключение «Заказ не найден»
    public NoOrderFoundException(String message) {
        super(message);
    }
}
