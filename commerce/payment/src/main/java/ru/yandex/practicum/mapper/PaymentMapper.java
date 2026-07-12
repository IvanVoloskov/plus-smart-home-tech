package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.entity.PaymentEntity;

@Component
public class PaymentMapper {
    public PaymentDto toDto(PaymentEntity entity) {
        if (entity == null) {
            return null;
        }
        return PaymentDto.builder()
                .paymentId(entity.getPaymentId())
                .totalPayment(entity.getTotalPayment())
                .deliveryTotal(entity.getTotalDelivery())
                .feeTotal(entity.getFeeTotal())
                .build();
    }
}
