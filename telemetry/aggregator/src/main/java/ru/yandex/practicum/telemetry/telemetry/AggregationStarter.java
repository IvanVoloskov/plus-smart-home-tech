package ru.yandex.practicum.telemetry.telemetry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.telemetry.service.AggregatorService;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final Producer<String, SpecificRecordBase> producer;
    private final AggregatorService aggregatorService;

    private static final String SENSORS_TOPIC = "telemetry.sensors.v1";
    private static final String SNAPSHOTS_TOPIC = "telemetry.snapshots.v1";
    @Value("${aggregator.kafka.consume-timeout-ms}")
    private long consumeTimeoutMs;

    public void start() {
        try {
            consumer.subscribe(List.of(SENSORS_TOPIC));

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records =
                        consumer.poll(Duration.ofMillis(consumeTimeoutMs));

                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    SensorEventAvro event = record.value();
                    log.info("Получено событие от датчика: {}", event.getId());

                    Optional<SensorsSnapshotAvro> snapshot =
                            aggregatorService.updateState(event);

                    snapshot.ifPresent(s -> producer.send(
                            new org.apache.kafka.clients.producer.ProducerRecord<>(
                                    SNAPSHOTS_TOPIC, s.getHubId(), s)));
                }
                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }
}
