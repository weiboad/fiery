package org.weiboad.ragnar.kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Properties;

public class KafkaUtil {
    private static KafkaProducer<String, String> kp;
    private static KafkaConsumer<String, String> kc;

    public static KafkaProducer<String, String> getProducer(String KafkaServers) {
        if (kp == null) {
            Properties props = new Properties();
            props.put("bootstrap.servers", KafkaServers);
            props.put("acks", "0");
            props.put("retries", 4);
            props.put("batch.size", 16384);
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            kp = new KafkaProducer<String, String>(props);
        }
        return kp;
    }


    public static KafkaConsumer<String, String> getConsumer() {
        if (kc == null) {
            Properties props = new Properties();

            props.put("bootstrap.servers", "10.1.78.23:9091,10.1.78.23:9092,10.1.78.23:9093");
            props.put("group.id", "12");
            props.put("enable.auto.commit", "true");
            props.put("auto.commit.interval.ms", "1000");
            props.put("session.timeout.ms", "30000");
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            kc = new KafkaConsumer<String, String>(props);
        }

        return kc;
    }
}
