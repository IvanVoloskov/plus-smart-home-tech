package ru.yandex.practicum.telemetry.collector.model.hub;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;

@Getter @Setter @ToString(callSuper = true)
public class DeviceAddedEvent extends HubEvent {
    String id;
    private DeviceTypeAvro deviceType;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_ADDED;
    }
}
