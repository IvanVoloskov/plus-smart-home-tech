package ru.yandex.practicum.telemetry.collector.model.hub.scenario;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class DeviceAction {
    private String sensor_id;
    private ActionType type;
    private Integer value;
}
