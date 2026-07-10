package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.entity.CartEntity;
import ru.yandex.practicum.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.feign.WarehouseFeignClient;
import ru.yandex.practicum.mapper.CartMapper;
import ru.yandex.practicum.model.ChangeProductQuantityRequest;
import ru.yandex.practicum.repository.CartRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {
    private final CartRepository cartRepository;
    private final CartMapper mapper;
    private final WarehouseFeignClient warehouseFeignClient;

    @Transactional
    public ShoppingCartDto getShoppingCart(String username) {
        validateUser(username);

        CartEntity cart = cartRepository.findByUsernameAndActiveTrue(username)
                .orElseGet(() -> createNewCart(username));

        return mapper.toDto(cart);
    }

    @Transactional
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        validateUser(username);

        CartEntity cart = cartRepository.findByUsernameAndActiveTrue(username)
                .orElseGet(() -> createNewCart(username));

        Map<UUID, Long> currentProducts = cart.getProducts();
        if (currentProducts == null) {
            currentProducts = new HashMap<>();
            cart.setProducts(currentProducts);
        }

        for(Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long quantity = entry.getValue();
            currentProducts.merge(productId, quantity, Long::sum);
        }

        cart.setProducts(currentProducts);

        ShoppingCartDto dtoForCheck = mapper.toDto(cart);
        warehouseFeignClient.checkProductQuantityEnoughForShoppingCart(dtoForCheck);

        CartEntity savedCart = cartRepository.save(cart);
        return mapper.toDto(savedCart);
    }

    @Transactional
    public void deactivateCurrentShoppingCart(String username) {
        validateUser(username);

        CartEntity cart = cartRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new NotAuthorizedUserException("Корзины не найдено у : " + username));

        cart.setActive(false);
        cartRepository.save(cart);
    }

    @Transactional
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {
        validateUser(username);

        CartEntity cart = cartRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new NotAuthorizedUserException("Корзины не найдено у : " + username));

        if (!cart.isActive()) {
            throw new NotAuthorizedUserException(String.format("Корзина пользователя %s неактивна", username));
        }

        Map<UUID, Long> currentProducts = cart.getProducts();
        if (currentProducts == null || currentProducts.isEmpty()) {
            throw new NoProductsInShoppingCartException("Корзина пользователя пустая");
        }

        boolean anyRemoved = false;
        for (UUID productId : productIds) {
            if (currentProducts.remove(productId) != null) {
                anyRemoved = true;
            }
        }

        if (!anyRemoved) {
            throw new NoProductsInShoppingCartException("В корзине не найдено ни одного из указанных товаров");
        }

        cart.setProducts(currentProducts);
        return mapper.toDto(cart);
    }

    @Transactional
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        validateUser(username);

        CartEntity cart = cartRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new NoProductsInShoppingCartException("Корзины не найдено у : " + username));

        Map<UUID, Long> currentProducts = cart.getProducts();
        if (currentProducts == null || currentProducts.isEmpty()) {
            throw new NoProductsInShoppingCartException("Корзина пользователя пустая");
        }

        UUID productId = request.getProductId();
        if (!currentProducts.containsKey(productId)) {
            throw new NoProductsInShoppingCartException("Товар с ID " + productId + " не найден в корзине");
        }

        Long newQuantity = request.getNewQuantity();
        if (newQuantity == null || newQuantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше 0");
        }

        currentProducts.put(productId, newQuantity);
        cart.setProducts(currentProducts);
        CartEntity savedCart = cartRepository.save(cart);
        return mapper.toDto(savedCart);
    }

    private void validateUser(String username) {
        if (username == null || username.isEmpty()) {
            throw new NotAuthorizedUserException("Имя пользователя не может быть пустым");
        }
    }

    private CartEntity createNewCart(String username) {
        CartEntity newCart = CartEntity.builder()
                .username(username)
                .products(new HashMap<>())
                .active(true)
                .build();
        return cartRepository.save(newCart);
    }
}
