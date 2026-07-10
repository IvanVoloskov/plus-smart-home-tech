package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.entity.PaymentEntity;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.feign.OrderFeignClient;
import ru.yandex.practicum.feign.StoreFeignClient;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.model.PaymentState;
import ru.yandex.practicum.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {
    private static final BigDecimal VAT_RATE = BigDecimal.valueOf(0.1);
    private final PaymentMapper mapper;
    private final PaymentRepository repository;
    private final OrderFeignClient orderFeignClient;
    private final StoreFeignClient storeFeignClient;

    @Transactional
    public PaymentDto payment(OrderDto order) {
        if (order.getProductPrice() == null || order.getDeliveryPrice() == null || order.getTotalPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Недостаточно информации в заказе для расчёта");
        }

        BigDecimal vat = order.getProductPrice().multiply(VAT_RATE);

        PaymentEntity payment = new PaymentEntity();
        payment.setOrderId(order.getOrderId());
        payment.setProductPrice(order.getProductPrice());
        payment.setTotalDelivery(order.getDeliveryPrice());
        payment.setTotalPayment(order.getTotalPrice());
        payment.setFeeTotal(vat);
        payment.setState(PaymentState.PENDING);
        repository.save(payment);
        return mapper.toDto(payment);
    }

    public BigDecimal getTotalCost(OrderDto order) {
        if (order.getProductPrice() == null || order.getDeliveryPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Недостаточно информации в заказе для расчёта");
        }
        BigDecimal VAT = order.getProductPrice().multiply(BigDecimal.valueOf(0.1));
        BigDecimal totalPriceProduct = VAT.add(order.getProductPrice());
        BigDecimal totalCostPrice = totalPriceProduct.add(order.getDeliveryPrice());
        return totalCostPrice;
    }

    @Transactional
    public void paymentSuccess(UUID paymentId) {
        PaymentEntity payment = getPaymentOrThrow(paymentId);
        payment.setState(PaymentState.SUCCESS);
        repository.save(payment);
        orderFeignClient.payment(payment.getOrderId());
    }

    @Transactional
    public void paymentFailed(UUID paymentId) {
        PaymentEntity payment = getPaymentOrThrow(paymentId);
        payment.setState(PaymentState.FAILED);
        repository.save(payment);
        orderFeignClient.paymentFailed(payment.getOrderId());
    }

    public BigDecimal productCost(OrderDto order) {
        Map<UUID, Long> products = order.getProducts();
        if (products == null || products.isEmpty()) {
            throw new NotEnoughInfoInOrderToCalculateException("В заказе отсутствуют товары для расчёта");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            ProductDto product = storeFeignClient.getProduct(entry.getKey());
            BigDecimal price = product.getPrice();
            total = total.add(price.multiply(BigDecimal.valueOf(entry.getValue())));
        }
        return total;
    }

    private PaymentEntity getPaymentOrThrow(UUID paymentId) {
        return repository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Оплата не найдена: " + paymentId));
    }
}
