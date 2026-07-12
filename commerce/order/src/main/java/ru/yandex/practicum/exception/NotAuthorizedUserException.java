package ru.yandex.practicum.exception;

public class NotAuthorizedUserException extends RuntimeException { // Исключение: пользователь не авторизован
    public NotAuthorizedUserException(String message) {
        super(message);
    }
}
