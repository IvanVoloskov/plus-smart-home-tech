package ru.yandex.practicum.telemetry.collector.model.hub;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.telemetry.collector.model.hub.scenario.DeviceAction;
import ru.yandex.practicum.telemetry.collector.model.hub.scenario.ScenarioCondition;

import java.util.List;

@Getter @Setter @ToString(callSuper = true)
public class ScenarioAddedEvent extends HubEvent {
    String name;
    List<ScenarioCondition> conditions;
    List<DeviceAction> actions;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED;
    }
}
