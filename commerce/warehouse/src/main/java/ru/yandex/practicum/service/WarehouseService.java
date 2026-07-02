package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.DimensionDto;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.entity.WarehouseEntity;
import ru.yandex.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.model.AddProductToWarehouseRequest;
import ru.yandex.practicum.model.NewProductInWarehouseRequest;
import ru.yandex.practicum.repository.WarehouseRepository;

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseService {
    private final WarehouseRepository repository;
    private final WarehouseMapper mapper;

    private static final String[] ADDRESSES = new String[]{"ADDRESS_1", "ADDRESS_2"};
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String CURRENT_ADDRESS =
            ADDRESSES[SECURE_RANDOM.nextInt(ADDRESSES.length)];

    @Transactional
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        if (repository.existsByProductId(request.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException("Ошибка, " +
                    "товар с таким описанием уже зарегистрирован на складе");
        } else {
            repository.save(mapper.toEntity(request));
        }
    }

    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto cart) {
        Map<UUID, Long> products = cart.getProducts();

        if (products == null || products.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouse("Корзина не может быть пустой");
        }

        // Один запрос к БД вместо N
        Map<UUID, WarehouseEntity> warehouseByProductId = repository
                .findAllByProductIdIn(products.keySet())
                .stream()
                .collect(Collectors.toMap(WarehouseEntity::getProductId, Function.identity()));

        double totalWeight = 0;
        double totalVolume = 0;
        boolean hasFragile = false;

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long requiredQuantity = entry.getValue();

            WarehouseEntity warehouseProduct = warehouseByProductId.get(productId);
            if (warehouseProduct == null) {
                throw new NoSpecifiedProductInWarehouseException(
                        "Товара из корзины нет на складе: " + productId);
            }

            if (warehouseProduct.getQuantity() < requiredQuantity) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(
                        "Недостаточное количество товара: " + productId +
                                ". Доступно: " + warehouseProduct.getQuantity() +
                                ", Нужно: " + requiredQuantity);
            }

            totalWeight += warehouseProduct.getWeight() * requiredQuantity;

            DimensionDto dimension = warehouseProduct.getDimension();
            double volume = dimension.getWidth() * dimension.getHeight() * dimension.getDepth();
            totalVolume += volume * requiredQuantity;

            if (warehouseProduct.isFragile()) {
                hasFragile = true;
            }
        }

        return BookedProductsDto.builder()
                .deliveryWeight(totalWeight)
                .deliveryVolume(totalVolume)
                .fragile(hasFragile)
                .build();
    }

    @Transactional
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        WarehouseEntity warehouseProduct = repository.findByProductId(request.getProductId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Данного товара нет на складе: "
                + request.getProductId()));

        warehouseProduct.setQuantity(warehouseProduct.getQuantity() + request.getQuantity());
        repository.save(warehouseProduct);
    }

    public AddressDto getWarehouseAddress() {
        return AddressDto.builder()
                .country(CURRENT_ADDRESS)
                .city(CURRENT_ADDRESS)
                .street(CURRENT_ADDRESS)
                .house(CURRENT_ADDRESS)
                .flat(CURRENT_ADDRESS)
                .build();
    }
}