package org.weiboad.ragnar.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Properties;

public class KafkaUtil {
    private static KafkaProducer<String, String> kp;

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

}
