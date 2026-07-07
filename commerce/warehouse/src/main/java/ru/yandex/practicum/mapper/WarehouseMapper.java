package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.entity.WarehouseEntity;
import ru.yandex.practicum.model.NewProductInWarehouseRequest;

@Component
public class WarehouseMapper {
    public WarehouseEntity toEntity(NewProductInWarehouseRequest request) {
        if (request == null) {
            return null;
        }

        return WarehouseEntity.builder()
                .productId(request.getProductId())
                .dimension(request.getDimension())
                .fragile(request.isFragile())
                .weight(request.getWeight())
                .quantity(0L)
                .build();
    }
}
