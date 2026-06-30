package ru.yandex.practicum.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.dto.QuantityState;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class SetProductQuantityStateRequest {
    private UUID productId;
    private QuantityState quantityState;
}
