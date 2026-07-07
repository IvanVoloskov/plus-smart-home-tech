package ru.yandex.practicum.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.dto.DimensionDto;

import java.util.UUID;

@Entity
@Table(name = "warehouse_products")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID productId;

    @Embedded
    private DimensionDto dimension;

    @Column(nullable = false)
    private double weight;

    @Column(nullable = false)
    private boolean fragile;

    @Column(nullable = false)
    private Long quantity;
}
