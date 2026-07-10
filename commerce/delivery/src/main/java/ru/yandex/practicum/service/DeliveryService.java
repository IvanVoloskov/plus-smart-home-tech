package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.DeliveryState;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.entity.DeliveryEntity;
import ru.yandex.practicum.exception.NoDeliveryFoundException;
import ru.yandex.practicum.feign.OrderFeignClient;
import ru.yandex.practicum.feign.WarehouseFeignClient;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.repository.DeliveryRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryService {
    private final DeliveryRepository repository;
    private final DeliveryMapper mapper;
    private final WarehouseFeignClient warehouseFeignClient;
    private final OrderFeignClient orderFeignClient;

    private static final BigDecimal BASE_COST = BigDecimal.valueOf(5);
    private static final BigDecimal ADDRESS_1 = BigDecimal.valueOf(1);
    private static final BigDecimal ADDRESS_2 = BigDecimal.valueOf(2);
    private static final BigDecimal FRAGILE = BigDecimal.valueOf(0.2);
    private static final BigDecimal WEIGHT_ORDER = BigDecimal.valueOf(0.3);
    private static final BigDecimal VOLUME_ORDER = BigDecimal.valueOf(0.2);
    private static final BigDecimal DIFFERENT_STREET = BigDecimal.valueOf(0.2);

    @Transactional
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        DeliveryEntity delivery = mapper.toEntity(deliveryDto);
        delivery.setDeliveryState(DeliveryState.CREATED);
        repository.save(delivery);
        return mapper.toDto(delivery);
    }

    public BigDecimal deliveryCost(OrderDto orderDto) {
        DeliveryEntity delivery = getByOrderIdOrThrow(orderDto.getOrderId());
        AddressDto warehouseAddress = warehouseFeignClient.getWarehouseAddress();
        BigDecimal totalCost = BASE_COST;

        BigDecimal warehouse = getWarehouseValue(warehouseAddress);
        totalCost = totalCost.add(BASE_COST.multiply(warehouse));

        if (Boolean.TRUE.equals(orderDto.getFragile())) {
            totalCost = totalCost.add(totalCost.multiply(FRAGILE));
        }

        if (orderDto.getDeliveryWeight() != null) {
            totalCost = totalCost.add(BigDecimal.valueOf(orderDto.getDeliveryWeight()).multiply(WEIGHT_ORDER));
        }

        if (orderDto.getDeliveryVolume() != null) {
            totalCost = totalCost.add(BigDecimal.valueOf(orderDto.getDeliveryVolume()).multiply(VOLUME_ORDER));
        }

        String streetOfWarehouse = warehouseAddress.getStreet();
        String streetOfDelivery = delivery.getToAddress().getStreet();
        boolean sameStreet = streetOfDelivery != null && streetOfDelivery.equals(streetOfWarehouse);

        if (!sameStreet) {
            totalCost = totalCost.add(totalCost.multiply(DIFFERENT_STREET));
        }

        return totalCost;

    }

    @Transactional
    public void deliverySuccessful(UUID orderId) {
        DeliveryEntity delivery = getByOrderIdOrThrow(orderId);

        delivery.setDeliveryState(DeliveryState.DELIVERED);
        repository.save(delivery);
        orderFeignClient.delivery(orderId);
    }

    @Transactional
    public void deliveryFailed(UUID orderId) {
        DeliveryEntity delivery = getByOrderIdOrThrow(orderId);

        delivery.setDeliveryState(DeliveryState.FAILED);
        repository.save(delivery);
        orderFeignClient.deliveryFailed(orderId);
    }

    @Transactional
    public void deliveryPicked(UUID orderId) {
        DeliveryEntity delivery = getByOrderIdOrThrow(orderId);

        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        repository.save(delivery);
        orderFeignClient.assembly(orderId);
    }

    private DeliveryEntity getByOrderIdOrThrow(UUID orderId) {
        return repository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException("Не найдена доставка"));
    }

    private BigDecimal getWarehouseValue(AddressDto address) {
        String street = address.getStreet();
        if (street != null && street.contains("ADDRESS_2")) {
            return ADDRESS_2;
        }
        return ADDRESS_1;
    }
}
