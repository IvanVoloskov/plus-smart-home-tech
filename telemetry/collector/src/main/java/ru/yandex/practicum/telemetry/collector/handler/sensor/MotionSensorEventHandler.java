package ru.yandex.practicum.telemetry.collector.handler.sensor;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.handler.SensorEventHandler;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class MotionSensorEventHandler implements SensorEventHandler {

    private final Producer<String, SpecificRecordBase> producer;
    private static final String SENSORS_TOPIC = "telemetry.sensors.v1";

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR;
    }

    @Override
    public void handler(SensorEventProto event) {
        SensorEventAvro avro = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(Instant.now())
                .setPayload(MotionSensorAvro.newBuilder()
                        .setLinkQuality(event.getMotionSensor().getLinkQuality())
                        .setMotion(event.getMotionSensor().getMotion())
                        .setVoltage(event.getMotionSensor().getVoltage())
                        .build())
                .build();
        producer.send(new ProducerRecord<>(SENSORS_TOPIC, event.getId(), avro));
    }
}
