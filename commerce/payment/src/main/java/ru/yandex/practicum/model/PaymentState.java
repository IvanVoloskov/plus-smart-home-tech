package ru.yandex.practicum.model;

public enum PaymentState {
    PENDING, // ожидает оплаты
    SUCCESS, // успешно оплачен
    FAILED // ошибка в процессе оплаты
}
