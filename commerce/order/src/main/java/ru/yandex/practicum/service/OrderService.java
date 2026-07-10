package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.DeliveryState;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.OrderState;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.entity.OrderEntity;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.feign.DeliveryFeignClient;
import ru.yandex.practicum.feign.PaymentFeignClient;
import ru.yandex.practicum.feign.WarehouseFeignClient;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.CreateNewOrderRequest;
import ru.yandex.practicum.model.ProductReturnRequest;
import ru.yandex.practicum.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderMapper mapper;
    private final OrderRepository repository;
    private final WarehouseFeignClient warehouseFeignClient;
    private final PaymentFeignClient paymentFeignClient;
    private final DeliveryFeignClient deliveryFeignClient;

    public List<OrderDto> getClientOrders(String username) {
        if (username == null || username.isEmpty()) {
            throw new NotAuthorizedUserException("Имя пользователя не может быть пустым или не заданным!");
        }
        List<OrderDto> orders = repository.findAllByUsername(username).stream().map(mapper::toDto).toList();
        return orders;
    }

    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest request, String username) {
        OrderEntity order = new OrderEntity();
        order.setUsername(username); // может быть null, если не передан
        order.setState(OrderState.NEW);
        order.setShoppingCartId(request.getShoppingCart().getShoppingCartId());
        order.setProducts(request.getShoppingCart().getProducts());
        repository.save(order); // получаем сгенерированный orderId

        AssemblyProductsForOrderRequest assemblyRequest =
                new AssemblyProductsForOrderRequest(order.getId(), order.getProducts());
        BookedProductsDto booked = warehouseFeignClient.assembly(assemblyRequest);
        order.setDeliveryWeight(booked.getDeliveryWeight());
        order.setDeliveryVolume(booked.getDeliveryVolume());
        order.setFragile(booked.isFragile());

        AddressDto warehouseAddress = warehouseFeignClient.getWarehouseAddress();
        DeliveryDto deliveryRequest = DeliveryDto.builder()
                .orderId(order.getId())
                .fromAddress(warehouseAddress)
                .toAddress(request.getDeliveryAddress())
                .deliveryState(DeliveryState.CREATED)
                .build();
        DeliveryDto delivery = deliveryFeignClient.planDelivery(deliveryRequest);
        order.setDeliveryId(delivery.getDeliveryId());

        repository.save(order);
        return mapper.toDto(order);
    }

    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        OrderEntity order = getOrderOrThrow(request.getOrderId());
        order.setState(OrderState.PRODUCT_RETURNED);
        repository.save(order);
        return mapper.toDto(order);
    }

    @Transactional
    public OrderDto payment(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);
        order.setState(OrderState.PAID);
        repository.save(order);
        return mapper.toDto(order);
    }

    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);
        order.setState(OrderState.PAYMENT_FAILED);
        repository.save(order);
        return mapper.toDto(order);
    }

    @Transactional
    public OrderDto delivery(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);
        order.setState(OrderState.DELIVERED);
        repository.save(order);
        return mapper.toDto(order);
    }

    @Transactional
    public OrderDto deliveryFailed(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);
        order.setState(OrderState.DELIVERY_FAILED);
        repository.save(order);
        return mapper.toDto(order);
    }

    @Transactional
    public OrderDto complete(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);
        order.setState(OrderState.COMPLETED);
        repository.save(order);
        return mapper.toDto(order);
    }

    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);
        OrderDto dto = mapper.toDto(order);

        BigDecimal productPrice = paymentFeignClient.productCost(dto);
        order.setProductPrice(productPrice);

        dto = mapper.toDto(order);
        BigDecimal totalPrice = paymentFeignClient.getTotalCost(dto);
        order.setTotalPrice(totalPrice);

        dto = mapper.toDto(order);
        PaymentDto payment = paymentFeignClient.payment(dto);
        order.setPaymentId(payment.getPaymentId());
        order.setState(OrderState.ON_PAYMENT);

        return mapper.toDto(repository.save(order));
    }

    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);
        OrderDto dto = mapper.toDto(order);

        BigDecimal deliveryPrice = deliveryFeignClient.deliveryCost(dto);
        order.setDeliveryPrice(deliveryPrice);

        return mapper.toDto(repository.save(order));
    }

    @Transactional
    public OrderDto assembly(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);
        order.setState(OrderState.ASSEMBLED);
        repository.save(order);
        return mapper.toDto(order);
    }

    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);
        repository.save(order);
        return mapper.toDto(order);
    }

    private OrderEntity getOrderOrThrow(UUID orderId) {
        return repository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Заказ не найден: " + orderId));
    }
}
