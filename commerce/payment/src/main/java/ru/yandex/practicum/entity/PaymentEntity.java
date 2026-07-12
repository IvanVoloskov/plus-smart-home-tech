package ru.yandex.practicum.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.model.PaymentState;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id")
    private UUID paymentId;
    @Column(name = "order_id")
    private UUID orderId;
    @Column(name = "product_price")
    private BigDecimal productPrice;
    @Column(name = "total_payment")
    private BigDecimal totalPayment;
    @Column(name = "total_delivery")
    private BigDecimal totalDelivery;
    @Column(name = "fee_total")
    private BigDecimal feeTotal;
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private PaymentState state;
}
