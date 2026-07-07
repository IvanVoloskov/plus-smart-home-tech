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
    double deliveryWeight; // Общий вес доставки
    double deliveryVolume; // Общие объём доставки
    boolean fragile; // Есть ли хрупкие вещи в доставке
}
