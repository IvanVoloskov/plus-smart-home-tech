package ru.yandex.practicum.telemetry.service;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.model.*;
import ru.yandex.practicum.telemetry.repository.ScenarioRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioAnalyzer {

    private final ScenarioRepository scenarioRepository;

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    public void analyze(SensorsSnapshotAvro snapshot) {
        List<Scenario> scenarios = scenarioRepository.findByHubId(snapshot.getHubId());

        for (Scenario scenario : scenarios) {
            boolean allConditionsMet = scenario.getConditions().stream()
                    .allMatch(condition -> checkCondition(condition, snapshot));

            if (allConditionsMet) {
                log.info("Сценарий '{}' активирован для хаба {}", scenario.getName(), snapshot.getHubId());
                executeActions(scenario, snapshot);
            }
        }
    }

    private boolean checkCondition(ScenarioCondition sc, SensorsSnapshotAvro snapshot) {
        String sensorId = sc.getSensor().getId();
        Condition condition = sc.getCondition();

        SensorStateAvro state = snapshot.getSensorsState().get(sensorId);
        if (state == null) return false;

        Integer sensorValue = extractValue(condition.getType(), state.getData());
        if (sensorValue == null) return false;

        return compare(sensorValue, condition.getValue(), condition.getOperation());
    }

    private Integer extractValue(ConditionType type, Object data) {
        return switch (type) {
            case TEMPERATURE -> {
                if (data instanceof TemperatureSensorAvro s) yield s.getTemperatureC();
                if (data instanceof ClimateSensorAvro s) yield s.getTemperatureC();
                yield null;
            }
            case HUMIDITY -> data instanceof ClimateSensorAvro s ? s.getHumidity() : null;
            case CO2LEVEL -> data instanceof ClimateSensorAvro s ? s.getCo2Level() : null;
            case LUMINOSITY -> data instanceof LightSensorAvro s ? s.getLuminosity() : null;
            case MOTION -> data instanceof MotionSensorAvro s ? (s.getMotion() ? 1 : 0) : null;
            case SWITCH -> data instanceof SwitchSensorAvro s ? (s.getState() ? 1 : 0) : null;
        };
    }

    private boolean compare(int sensorValue, int conditionValue, ConditionOperation operation) {
        return switch (operation) {
            case EQUALS -> sensorValue == conditionValue;
            case GREATER_THAN -> sensorValue > conditionValue;
            case LOWER_THAN -> sensorValue < conditionValue;
        };
    }

    private void executeActions(Scenario scenario, SensorsSnapshotAvro snapshot) {
        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();

        for (ScenarioAction sa : scenario.getActions()) {
            Action action = sa.getAction();
            DeviceActionProto deviceAction = DeviceActionProto.newBuilder()
                    .setSensorId(sa.getSensor().getId())
                    .setType(ActionTypeProto.valueOf(action.getType().name()))
                    .setValue(action.getValue() != null ? action.getValue() : 0)
                    .build();

            DeviceActionRequest request = DeviceActionRequest.newBuilder()
                    .setHubId(snapshot.getHubId())
                    .setScenarioName(scenario.getName())
                    .setAction(deviceAction)
                    .setTimestamp(timestamp)
                    .build();

            log.info("Отправляю действие {} для хаба {}", action.getType(), snapshot.getHubId());
            hubRouterClient.handleDeviceAction(request);
        }
    }
}