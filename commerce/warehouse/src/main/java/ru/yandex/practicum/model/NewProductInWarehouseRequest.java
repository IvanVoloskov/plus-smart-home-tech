package ru.yandex.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.dto.DimensionDto;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewProductInWarehouseRequest { // Запрос на добавление нового товара на склад
    private UUID productId;
    private boolean fragile;
    private DimensionDto dimension;
    private double weight;
}
