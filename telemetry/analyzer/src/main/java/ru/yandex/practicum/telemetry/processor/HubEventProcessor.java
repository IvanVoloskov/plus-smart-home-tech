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
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);

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

            List<Condition> conditions = e.getConditions().stream()
                    .map(c -> {
                        Condition condition = new Condition();
                        condition.setType(ConditionType.valueOf(c.getType().name()));
                        condition.setOperation(ConditionOperation.valueOf(c.getOperation().name()));
                        if (c.getValue() instanceof Integer v) {
                            condition.setValue(v);
                        }
                        return conditionRepository.save(condition);
                    }).toList();

            List<Action> actions = e.getActions().stream()
                    .map(a -> {
                        Action action = new Action();
                        action.setType(ActionType.valueOf(a.getType().name()));
                        action.setValue(a.getValue());
                        return actionRepository.save(action);
                    }).toList();

            scenario.setConditions(conditions);
            scenario.setActions(actions);
            scenarioRepository.save(scenario);

        } else if (payload instanceof ScenarioRemovedEventAvro e) {
            scenarioRepository.findByHubIdAndName(event.getHubId(), e.getName())
                    .ifPresent(scenarioRepository::delete);
        }
    }
}
