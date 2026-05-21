package ru.yandex.practicum.telemetry.collector.model.hub.scenario;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScenarioCondition {
    private String sensor_id;
    private ConditionType type;
    private ConditionOperation operation;
    private Integer value;
}
