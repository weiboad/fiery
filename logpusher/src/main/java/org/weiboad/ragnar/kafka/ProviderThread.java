package org.weiboad.ragnar.kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

//http://blog.csdn.net/stonexmx/article/details/52326388

public class ProviderThread extends Thread {

    private Logger log = LoggerFactory.getLogger(ProviderThread.class);

    private Callback callback;

    private String serverList = "";

    private String kafkaTopic = "";

    private ConcurrentLinkedQueue<String> commonLogQueue;

    private ConcurrentLinkedQueue<String> metaLogQueue;

    public ProviderThread(String kafkaTopic, String serverList, ConcurrentLinkedQueue<String> metaLogQueue, ConcurrentLinkedQueue<String> commonLogQueue) {

        if (metaLogQueue == null) {
            log.error("meta queue obj is null...");
            System.exit(7);
        }

        if (commonLogQueue == null) {
            log.error("common queue obj is null...");
            System.exit(8);
        }

        if (serverList == null || serverList.length() == 0) {
            log.error("kafka server list is empty...");
            System.exit(9);
        }

        if (kafkaTopic == null || kafkaTopic.length() == 0) {
            log.error("kafka topic is empty...");
            System.exit(4);
        }

        callback = new Callback() {
            public void onCompletion(RecordMetadata metadata, Exception e) {
                if (e != null) {
                    e.printStackTrace();
                    log.error("message send to partition " + metadata.partition() + ", offset: " + metadata.offset());
                }
            }
        };

        this.metaLogQueue = metaLogQueue;
        this.commonLogQueue = commonLogQueue;
        this.serverList = serverList;
        this.kafkaTopic = kafkaTopic;

    }

    public void run() {

        while (true) {
            //while for the crash will recovery

            try {
                Producer<String, String> producer = KafkaUtil.getProducer(this.serverList);
                while (true) {
                    String contentLog = commonLogQueue.poll();

                    if (contentLog != null && contentLog.length() > 0) {
                        String[] contentList = contentLog.split("\n");
                        for (int index = 0; index < contentList.length; index++) {
                            ProducerRecord<String, String> record = new ProducerRecord<String, String>(kafkaTopic, null, contentList[index]);
                            producer.send(record);
                        }
                    }

                    String metaLog = metaLogQueue.poll();

                    if (metaLog != null && metaLog.length() > 0) {
                        String[] metaList = metaLog.split("\n");

                        for (int index = 0; index < metaList.length; index++) {
                            ProducerRecord<String, String> record = new ProducerRecord<String, String>(kafkaTopic, null, metaList[index]);
                            producer.send(record);
                        }
                    }

                    if (contentLog == null && metaLog == null) {
                        Thread.sleep(100);
                    } else {
                        Thread.sleep(10);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("kafka send error" + e.getMessage());
            }
        }

    }
}