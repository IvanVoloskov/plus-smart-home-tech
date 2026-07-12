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
    private UUID paymentId; // Идентификатор оплаты
    private BigDecimal totalPayment; // Общая стоимость
    private BigDecimal deliveryTotal; // стоимость доставки
    private BigDecimal feeTotal; // стоимость налога
}