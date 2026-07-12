package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.model.CreateNewOrderRequest;
import ru.yandex.practicum.model.ProductReturnRequest;
import ru.yandex.practicum.service.OrderService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public List<OrderDto> getClientOrders(@RequestParam String username) {
        return orderService.getClientOrders(username);
    }

    @PutMapping
    public OrderDto createNewOrder(@RequestBody CreateNewOrderRequest request,
                                   @RequestParam (required = false) String username) {
        return orderService.createNewOrder(request, username);
    }

    @PostMapping("/return")
    public OrderDto productReturn(@RequestBody ProductReturnRequest request){
        return orderService.productReturn(request);
    }

    @PostMapping("/payment")
    public OrderDto payment(UUID orderId) {
        return orderService.payment(orderId);
    }

    @PostMapping("/payment/failed")
    public OrderDto paymentFailed(UUID orderId) {
        return orderService.paymentFailed(orderId);
    }

    @PostMapping("/delivery")
    public OrderDto delivery(UUID orderId) {
        return orderService.delivery(orderId);
    }

    @PostMapping("/delivery/failed")
    public OrderDto deliveryFailed(UUID orderId) {
        return orderService.deliveryFailed(orderId);
    }

    @PostMapping("/completed")
    public OrderDto complete(UUID orderId) {
        return orderService.complete(orderId);
    }

    @PostMapping("/calculate/total")
    public OrderDto calculateTotalCost(UUID orderId) {
        return orderService.calculateTotalCost(orderId);
    }

    @PostMapping("/calculate/delivery")
    public OrderDto calculateDeliveryCost(UUID orderId) {
        return orderService.calculateDeliveryCost(orderId);
    }

    @PostMapping("/assembly")
    public OrderDto assembly(UUID orderId) {
        return orderService.assembly(orderId);
    }

    @PostMapping("/assembly/failed")
    public OrderDto assemblyFailed(UUID orderId) {
        return orderService.assemblyFailed(orderId);
    }
}
