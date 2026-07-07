package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.entity.CartEntity;

@Component
public class CartMapper {
    public ShoppingCartDto toDto(CartEntity entity) {
        if (entity == null) {
            return null;
        }

        return ShoppingCartDto.builder()
                .shoppingCartId(entity.getShoppingCartId())
                .products(entity.getProducts())
                .build();
    }

    public CartEntity toEntity(ShoppingCartDto dto) {
        if (dto == null) {
            return null;
        }

        return CartEntity.builder()
                .shoppingCartId(dto.getShoppingCartId())
                .products(dto.getProducts())
                .build();
    }
}
