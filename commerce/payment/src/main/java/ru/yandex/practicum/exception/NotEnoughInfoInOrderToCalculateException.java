package ru.yandex.practicum.exception;
// Исключение: недостаточно данных для выполнения расчета
public class NotEnoughInfoInOrderToCalculateException extends RuntimeException {
    public NotEnoughInfoInOrderToCalculateException(String message) {
        super(message);
    }
}
