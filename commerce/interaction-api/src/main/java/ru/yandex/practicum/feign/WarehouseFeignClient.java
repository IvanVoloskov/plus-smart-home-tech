package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.ShippedToDeliveryRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;

import java.util.Map;
import java.util.UUID;


@FeignClient(name = "warehouse")
public interface WarehouseFeignClient {
    @PostMapping("/api/v1/warehouse/check")
    BookedProductsDto checkProductQuantityEnoughForShoppingCart(@RequestBody ShoppingCartDto shoppingCartDto);

    @GetMapping("/api/v1/warehouse/address")
    AddressDto getWarehouseAddress();

    @PostMapping("/api/v1/warehouse/assembly")
    BookedProductsDto assembly(@RequestBody AssemblyProductsForOrderRequest request);

    @PostMapping("/api/v1/warehouse/shipped")
    void shippedToDelivery(@RequestBody ShippedToDeliveryRequest request);

    @PostMapping("/api/v1/warehouse/return")
    void returnProducts(@RequestBody Map<UUID, Long> products);
}
