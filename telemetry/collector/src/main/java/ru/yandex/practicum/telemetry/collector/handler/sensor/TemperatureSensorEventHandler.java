package ru.yandex.practicum.telemetry.collector.handler.sensor;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.telemetry.collector.handler.SensorEventHandler;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class TemperatureSensorEventHandler implements SensorEventHandler {
    private final Producer<String, SpecificRecordBase> producer;
    private static final String SENSORS_TOPIC = "telemetry.sensors.v1";


    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR;
    }

    @Override
    public void handler(SensorEventProto event) {
        SensorEventAvro avro = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(
                        event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()))
                .setPayload(TemperatureSensorAvro.newBuilder()
                        .setTemperatureC(event.getTemperatureSensor().getTemperatureC())
                        .setTemperatureF(event.getTemperatureSensor().getTemperatureF())
                        .build())
                .build();
        producer.send(new ProducerRecord<>(SENSORS_TOPIC, event.getId(), avro));
    }
}
