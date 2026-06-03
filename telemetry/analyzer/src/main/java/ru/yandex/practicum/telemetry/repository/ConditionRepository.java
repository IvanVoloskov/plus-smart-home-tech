package ru.yandex.practicum.telemetry.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.telemetry.model.Condition;

public interface ConditionRepository extends JpaRepository<Condition, Long> {
}
