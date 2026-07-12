package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.entity.DeliveryEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<DeliveryEntity, UUID> {
    Optional<DeliveryEntity> findByOrderId(UUID orderId);
}
