package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.entity.OrderBookingEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderBookingRepository extends JpaRepository<OrderBookingEntity, UUID> {

    Optional<OrderBookingEntity> findByOrderId(UUID orderId);
}
