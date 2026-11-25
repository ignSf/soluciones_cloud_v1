package com.example.inventory.kafka;

import com.example.inventory.config.KafkaTopicConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class DeadLetterLogger {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterLogger.class);

    @KafkaListener(topics = KafkaTopicConfig.ORDERS_DLT,
            groupId = "inventory-dlt-group",
            properties = "value.deserializer:org.apache.kafka.common.serialization.StringDeserializer")
    public void onDeadLetter(ConsumerRecord<String, String> record) {
        log.error("DEAD LETTER en {} | origen: {} | causa: {} | payload: {}",
                record.topic(),
                header(record, KafkaHeaders.DLT_ORIGINAL_TOPIC),
                header(record, KafkaHeaders.DLT_EXCEPTION_MESSAGE),
                record.value());
    }

    private static String header(ConsumerRecord<?, ?> record, String name) {
        Header h = record.headers().lastHeader(name);
        return h == null ? "n/d" : new String(h.value(), StandardCharsets.UTF_8);
    }
}