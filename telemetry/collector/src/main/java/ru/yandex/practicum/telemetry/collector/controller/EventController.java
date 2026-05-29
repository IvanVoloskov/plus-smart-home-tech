package ru.yandex.practicum.telemetry.collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.service.CollectorService;

@RestController
@RequiredArgsConstructor
public class EventController {
    private final CollectorService collectorService;

    @PostMapping("/events/sensors")
    public void collectSensorsEvent(@Valid @RequestBody SensorEvent event) {
        collectorService.collect(event);
    }

    @PostMapping("/events/hubs")
    public void collectHubsEvent(@Valid @RequestBody HubEvent event) {
        collectorService.collect(event);
    }
}
