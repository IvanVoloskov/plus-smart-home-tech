package ru.yandex.practicum.telemetry.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.model.*;
import ru.yandex.practicum.telemetry.repository.*;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    private final KafkaConsumer<String, HubEventAvro> hubEventConsumer;
    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;

    private static final String HUBS_TOPIC = "telemetry.hubs.v1";
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(100);

    @Override
    public void run() {
        try {
            hubEventConsumer.subscribe(List.of(HUBS_TOPIC));

            while (true) {
                ConsumerRecords<String, HubEventAvro> records =
                        hubEventConsumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    HubEventAvro event = record.value();
                    log.info("Получено событие хаба: {}", event.getHubId());
                    handleHubEvent(event);
                }
                hubEventConsumer.commitAsync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка обработки событий хаба", e);
        } finally {
            try {
                hubEventConsumer.commitSync();
            } finally {
                hubEventConsumer.close();
            }
        }
    }

    private void handleHubEvent(HubEventAvro event) {
        Object payload = event.getPayload();

        if (payload instanceof DeviceAddedEventAvro e) {
            Sensor sensor = new Sensor();
            sensor.setId(e.getId().toString());
            sensor.setHubId(event.getHubId());
            sensorRepository.save(sensor);

        } else if (payload instanceof DeviceRemovedEventAvro e) {
            sensorRepository.findByIdAndHubId(e.getId().toString(), event.getHubId())
                    .ifPresent(sensorRepository::delete);

        } else if (payload instanceof ScenarioAddedEventAvro e) {
            Scenario scenario = scenarioRepository
                    .findByHubIdAndName(event.getHubId(), e.getName())
                    .orElse(new Scenario());

            scenario.setHubId(event.getHubId());
            scenario.setName(e.getName());
            scenarioRepository.save(scenario);

            // Удаляем старые условия и действия если сценарий уже существовал
            if (scenario.getConditions() != null) scenario.getConditions().clear();
            if (scenario.getActions() != null) scenario.getActions().clear();

            for (ScenarioConditionAvro c : e.getConditions()) {
                Sensor sensor = sensorRepository.findById(c.getSensorId())
                        .orElseGet(() -> {
                            Sensor s = new Sensor();
                            s.setId(c.getSensorId());
                            s.setHubId(event.getHubId());
                            return sensorRepository.save(s);
                        });

                Condition condition = new Condition();
                condition.setType(ConditionType.valueOf(c.getType().name()));
                condition.setOperation(ConditionOperation.valueOf(c.getOperation().name()));
                if (c.getValue() instanceof Integer v) {
                    condition.setValue(v);
                }
                conditionRepository.save(condition);

                ScenarioCondition sc = new ScenarioCondition();
                ScenarioConditionId scId = new ScenarioConditionId();
                scId.setScenarioId(scenario.getId());
                scId.setSensorId(sensor.getId());
                scId.setConditionId(condition.getId());
                sc.setId(scId);
                sc.setScenario(scenario);
                sc.setSensor(sensor);
                sc.setCondition(condition);
                scenario.getConditions().add(sc);
            }

            for (DeviceActionAvro a : e.getActions()) {
                Sensor sensor = sensorRepository.findById(a.getSensorId())
                        .orElseGet(() -> {
                            Sensor s = new Sensor();
                            s.setId(a.getSensorId());
                            s.setHubId(event.getHubId());
                            return sensorRepository.save(s);
                        });

                Action action = new Action();
                action.setType(ActionType.valueOf(a.getType().name()));
                action.setValue(a.getValue());
                actionRepository.save(action);

                ScenarioAction sa = new ScenarioAction();
                ScenarioActionId saId = new ScenarioActionId();
                saId.setScenarioId(scenario.getId());
                saId.setSensorId(sensor.getId());
                saId.setActionId(action.getId());
                sa.setId(saId);
                sa.setScenario(scenario);
                sa.setSensor(sensor);
                sa.setAction(action);
                scenario.getActions().add(sa);
            }

        scenarioRepository.save(scenario);

            log.info("Сохранён сценарий '{}' hub={}, conditions={}, actions={}",
                    scenario.getName(), event.getHubId(), scenario.getConditions().size(), scenario.getActions().size());
        }
    }
}
