package ru.yandex.practicum.feign;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;

import java.math.BigDecimal;

@FeignClient(name = "payment")
public interface PaymentFeignClient {

    @PostMapping("/api/v1/payment/productCost")
    BigDecimal productCost(@RequestBody OrderDto order);

    @PostMapping("/api/v1/payment//totalCost")
    BigDecimal getTotalCost(@RequestBody OrderDto order);

    @PostMapping("/api/v1/payment")
    PaymentDto payment(@RequestBody OrderDto order);
}
