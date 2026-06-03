package ru.yandex.practicum.telemetry.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceRemovedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioRemovedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.scenario.DeviceAction;
import ru.yandex.practicum.telemetry.collector.model.hub.scenario.ScenarioCondition;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.model.sensor.ClimateSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.LightSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.MotionSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SwitchSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.TemperatureSensorEvent;

import java.time.Instant;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorService {

    private final Producer<String, SpecificRecordBase> producer;

    private static final String SENSORS_TOPIC = "telemetry.sensors.v1";
    private static final String HUBS_TOPIC = "telemetry.hubs.v1";

    public void collect(SensorEvent event) {
        log.info("Получено событие датчика: {}", event);
        SensorEventAvro avroEvent = toSensorAvro(event);
        producer.send(new ProducerRecord<>(SENSORS_TOPIC, event.getId(), avroEvent));
    }

    public void collect(HubEvent event) {
        log.info("Получено событие хаба: {}", event);
        HubEventAvro avroEvent = toHubAvro(event);
        producer.send(new ProducerRecord<>(HUBS_TOPIC, event.getHubId(), avroEvent));
    }

    public void collect(SensorEventProto event) {
        log.info("Получено gRPC событие датчика: {}", event);
        SensorEventAvro avroEvent = toSensorAvroFromProto(event);
        producer.send(new ProducerRecord<>(SENSORS_TOPIC, event.getId(), avroEvent));
    }

    public void collect(HubEventProto event) {
        log.info("Получено gRPC событие хаба: {}", event);
        HubEventAvro avroEvent = toHubAvroFromProto(event);
        producer.send(new ProducerRecord<>(HUBS_TOPIC, event.getHubId(), avroEvent));
    }

    private SensorEventAvro toSensorAvro(SensorEvent event) {
        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(buildSensorPayload(event))
                .build();
    }

    private Object buildSensorPayload(SensorEvent event) {
        if (event instanceof ClimateSensorEvent e) {
            return ClimateSensorAvro.newBuilder()
                    .setTemperatureC(e.getTemperatureC())
                    .setHumidity(e.getHumidity())
                    .setCo2Level(e.getCo2Level())
                    .build();
        }
        if (event instanceof LightSensorEvent e) {
            return LightSensorAvro.newBuilder()
                    .setLinkQuality(e.getLinkQuality())
                    .setLuminosity(e.getLuminosity())
                    .build();
        }
        if (event instanceof MotionSensorEvent e) {
            return MotionSensorAvro.newBuilder()
                    .setLinkQuality(e.getLinkQuality())
                    .setMotion(e.isMotion())
                    .setVoltage(e.getVoltage())
                    .build();
        }
        if (event instanceof SwitchSensorEvent e) {
            return SwitchSensorAvro.newBuilder()
                    .setState(e.isState())
                    .build();
        }
        if (event instanceof TemperatureSensorEvent e) {
            return TemperatureSensorAvro.newBuilder()
                    .setTemperatureC(e.getTemperatureC())
                    .setTemperatureF(e.getTemperatureF())
                    .build();
        }
        throw new IllegalArgumentException("Неизвестный тип датчика: " + event.getType());
    }

    private HubEventAvro toHubAvro(HubEvent event) {
        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(buildHubPayload(event))
                .build();
    }

    private Object buildHubPayload(HubEvent event) {
        if (event instanceof DeviceAddedEvent e) {
            return DeviceAddedEventAvro.newBuilder()
                    .setId(e.getId())
                    .setType(e.getDeviceType())
                    .build();
        }
        if (event instanceof DeviceRemovedEvent e) {
            return DeviceRemovedEventAvro.newBuilder()
                    .setId(e.getId())
                    .build();
        }
        if (event instanceof ScenarioAddedEvent e) {
            return ScenarioAddedEventAvro.newBuilder()
                    .setName(e.getName())
                    .setConditions(toConditionsAvro(e.getConditions()))
                    .setActions(toActionsAvro(e.getActions()))
                    .build();
        }
        if (event instanceof ScenarioRemovedEvent e) {
            return ScenarioRemovedEventAvro.newBuilder()
                    .setName(e.getName())
                    .build();
        }
        throw new IllegalArgumentException("Неизвестный тип события хаба: " + event.getType());
    }

    private List<ScenarioConditionAvro> toConditionsAvro(List<ScenarioCondition> conditions) {
        return conditions.stream()
                .map(c -> ScenarioConditionAvro.newBuilder()
                        .setSensorId(c.getSensorId())
                        .setType(ConditionTypeAvro.valueOf(c.getType().name()))
                        .setOperation(ConditionOperationAvro.valueOf(c.getOperation().name()))
                        .setValue(c.getValue())
                        .build())
                .toList();
    }

    private List<DeviceActionAvro> toActionsAvro(List<DeviceAction> actions) {
        return actions.stream()
                .map(a -> DeviceActionAvro.newBuilder()
                        .setSensorId(a.getSensorId())
                        .setType(ActionTypeAvro.valueOf(a.getType().name()))
                        .setValue(a.getValue())
                        .build())
                .toList();
    }

    private SensorEventAvro toSensorAvroFromProto(SensorEventProto event) {
        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(
                        event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()))
                .setPayload(buildSensorPayloadFromProto(event))
                .build();
    }

    private Object buildSensorPayloadFromProto(SensorEventProto event) {
        return switch (event.getPayloadCase()) {
            case CLIMATE_SENSOR -> ClimateSensorAvro.newBuilder()
                    .setTemperatureC(event.getClimateSensor().getTemperatureC())
                    .setHumidity(event.getClimateSensor().getHumidity())
                    .setCo2Level(event.getClimateSensor().getCo2Level())
                    .build();
            case LIGHT_SENSOR -> LightSensorAvro.newBuilder()
                    .setLinkQuality(event.getLightSensor().getLinkQuality())
                    .setLuminosity(event.getLightSensor().getLuminosity())
                    .build();
            case MOTION_SENSOR -> MotionSensorAvro.newBuilder()
                    .setLinkQuality(event.getMotionSensor().getLinkQuality())
                    .setMotion(event.getMotionSensor().getMotion())
                    .setVoltage(event.getMotionSensor().getVoltage())
                    .build();
            case SWITCH_SENSOR -> SwitchSensorAvro.newBuilder()
                    .setState(event.getSwitchSensor().getState())
                    .build();
            case TEMPERATURE_SENSOR -> TemperatureSensorAvro.newBuilder()
                    .setTemperatureC(event.getTemperatureSensor().getTemperatureC())
                    .setTemperatureF(event.getTemperatureSensor().getTemperatureF())
                    .build();
            default -> throw new IllegalArgumentException(
                    "Неизвестный тип датчика: " + event.getPayloadCase());
        };
    }

    private HubEventAvro toHubAvroFromProto(HubEventProto event) {
        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(
                        event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()))
                .setPayload(buildHubPayloadFromProto(event))
                .build();
    }

    private Object buildHubPayloadFromProto(HubEventProto event) {
        return switch (event.getPayloadCase()) {
            case DEVICE_ADDED -> DeviceAddedEventAvro.newBuilder()
                    .setId(event.getDeviceAdded().getId())
                    .setType(DeviceTypeAvro.valueOf(event.getDeviceAdded().getType().name()))
                    .build();
            case DEVICE_REMOVED -> DeviceRemovedEventAvro.newBuilder()
                    .setId(event.getDeviceRemoved().getId())
                    .build();
            case SCENARIO_ADDED -> ScenarioAddedEventAvro.newBuilder()
                    .setName(event.getScenarioAdded().getName())
                    .setConditions(List.of())
                    .setActions(List.of())
                    .build();
            case SCENARIO_REMOVED -> ScenarioRemovedEventAvro.newBuilder()
                    .setName(event.getScenarioRemoved().getName())
                    .build();
            default -> throw new IllegalArgumentException(
                    "Неизвестный тип события хаба: " + event.getPayloadCase());
        };
    }
}