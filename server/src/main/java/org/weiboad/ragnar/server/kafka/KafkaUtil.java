package org.weiboad.ragnar.server.kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Properties;

public class KafkaUtil {
    private static KafkaConsumer<String, String> kc;

    public static KafkaConsumer<String, String> getConsumer(String serverList, String groupID) {
        if (kc == null) {
            Properties props = new Properties();

            props.put("bootstrap.servers", serverList);
            props.put("group.id", groupID);
            props.put("enable.auto.commit", "true");
            props.put("auto.commit.interval.ms", "1000");
            props.put("session.timeout.ms", "30000");
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            kc = new KafkaConsumer<String, String>(props);
        }

        return kc;
    }
    public static void cleanConsumer() {
        kc = null;
    }

}
