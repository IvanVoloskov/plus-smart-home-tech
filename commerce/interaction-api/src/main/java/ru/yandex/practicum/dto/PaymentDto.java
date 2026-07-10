package ru.yandex.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {
    UUID paymentId; // Идентификатор оплаты
    BigDecimal totalPayment; // Общая стоимость
    BigDecimal deliveryTotal; // стоимость доставки
    BigDecimal feeTotal; // стоимость налога
}
