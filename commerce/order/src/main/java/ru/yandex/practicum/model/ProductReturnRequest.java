package ru.yandex.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReturnRequest { // Запрос на возврат товара
    UUID orderId;
    Map<UUID, Long> products;
}
