package org.weiboad.ragnar.server.kafka;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.weiboad.ragnar.server.config.FieryConfig;
import org.weiboad.ragnar.server.processor.BizLogProcessor;
import org.weiboad.ragnar.server.processor.MetaLogProcessor;
import org.weiboad.ragnar.server.search.IndexService;
import org.weiboad.ragnar.server.struct.MetaLog;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
@Scope("singleton")
public class Consumer implements DisposableBean, Runnable {

    @Autowired
    private FieryConfig fieryConfig;

    @Autowired
    BizLogProcessor bizLogProcessor;

    @Autowired
    MetaLogProcessor metaLogProcessor;

    @Autowired
    IndexService indexHelper;

    private Thread thread;

    private Logger log = LoggerFactory.getLogger(Consumer.class);

    private Map<Integer, Long> parationOffset = new HashMap<>();

    private KafkaConsumer<String, String> consumer;

    private boolean isDestroy = false;

    @PostConstruct
    public void start() {
        if (fieryConfig.getKafkaenable()) {
            log.info("start kafka Consumer...");
            this.startConsumer(false);
        }
    }

    public boolean startConsumer(boolean startWithOffset) {
        if (this.thread == null || !this.thread.isAlive()) {
            log.info("start kafka Log Consumer Thread...");

            this.isDestroy = false;
            this.thread = new Thread(this);
            this.thread.start();
            return true;
        }

        log.error("kafka consumer thread is alive...");
        return false;
    }


    /**
     * get paration offset map
     *
     * @return Map
     */
    public Map<Integer, Long> getParationOffset() {
        return this.parationOffset;
    }

    @Override
    public void run() {
        if (fieryConfig.getKafkaenable()) {
            try {

                //when the queue is full pause the queue
                boolean isPause = false;

                List<String> topicList = new ArrayList<>();
                topicList.add(fieryConfig.getKafkatopic());

                consumer = KafkaUtil.getConsumer(fieryConfig.getKafkaserver(), fieryConfig.getKafkagroupid());
                consumer.subscribe(topicList);
                while (!this.isDestroy) {

                    List<PartitionInfo> parationList = consumer.partitionsFor(fieryConfig.getKafkatopic());

                    //not avalible pause poll
                    if ((!bizLogProcessor.checkAvalible() || !metaLogProcessor.checkAvalible()) && !isPause) {
                        for (PartitionInfo partitionInfo : parationList) {
                            int partitionid = partitionInfo.partition();
                            TopicPartition partition = new TopicPartition(fieryConfig.getKafkatopic(), partitionid);
                            consumer.pause(partition);
                        }
                        isPause = true;
                    }

                    //avalible on queue resume poll
                    if ((bizLogProcessor.checkAvalible() && metaLogProcessor.checkAvalible()) && isPause) {
                        for (PartitionInfo partitionInfo : parationList) {
                            int partitionid = partitionInfo.partition();
                            TopicPartition partition = new TopicPartition(fieryConfig.getKafkatopic(), partitionid);
                            consumer.resume(partition);
                        }
                        isPause = false;
                    }

                    ConsumerRecords<String, String> records = consumer.poll(1000);

                    for (ConsumerRecord<String, String> record : records) {

                        //partion offset record
                        parationOffset.put(record.partition(), record.offset());

                        //log.info("fetched from partition " + record.partition() + ", offset: " + record.offset() + ", message: " + record.value());
                        String content = record.value();

                        if (content.trim().length() == 0) {
                            continue;
                        }

                        //is meta log?
                        if (!content.substring(0, 1).equals("[")) {
                            //metalog
                            Gson gsonHelper = new Gson();

                            //base64 decode
                            try {
                                sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
                                content = new String(decoder.decodeBuffer(content));
                                String[] metalogPack = content.trim().split("\n");

                                //remove the es info
                                if (metalogPack.length == 2) {
                                    MetaLog metalog = gsonHelper.fromJson(metalogPack[1], MetaLog.class);
                                    indexHelper.insertProcessQueue(metalog);
                                    metaLogProcessor.insertDataQueue(metalog);
                                }

                            } catch (Exception e) {
                                log.error("parser json:" + content);
                                e.printStackTrace();
                            }

                        } else {
                            //common log
                            JsonParser valueParse = new JsonParser();

                            try {
                                JsonArray valueArr = (JsonArray) valueParse.parse(content);
                                bizLogProcessor.insertDataQueue(valueArr);
                            } catch (Exception e) {
                                e.printStackTrace();
                                log.error("kafka parser json wrong:" + content);
                            }
                        }
                    }

                }
            } catch (WakeupException e) {
                // Ignore exception if closing
                if (!this.isDestroy) {
                    throw e;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                consumer.close();
                this.consumer = null;
                KafkaUtil.cleanConsumer();
            }
        }
    }


    /**
     * stop consumer
     */
    public void shutdown() {
        log.info("shutdown the consumer threat");
        this.isDestroy = true;
        consumer.wakeup();
        try {
            this.thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        this.shutdown();
    }

}
