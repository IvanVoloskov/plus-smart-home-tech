package ru.yandex.practicum.telemetry.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.telemetry.model.Action;

public interface ActionRepository extends JpaRepository<Action, Long> {
}
