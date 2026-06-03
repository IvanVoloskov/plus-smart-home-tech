package ru.yandex.practicum.telemetry.collector.handler;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

public interface HubEventHandler {
    HubEventProto.PayloadCase getMessageType();
    void handler(HubEventProto event);
}
