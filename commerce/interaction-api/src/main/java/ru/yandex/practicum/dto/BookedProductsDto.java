package ru.yandex.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookedProductsDto {
    private double deliveryWeight; // Общий вес доставки
    private double deliveryVolume; // Общие объём доставки
    private boolean fragile; // Есть ли хрупкие вещи в доставке
}
