package org.weiboad.ragnar.server.kafka;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
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
import java.util.Arrays;

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

    @PostConstruct
    public void start() {
        if (fieryConfig.getKafkaenable()) {
            log.info("start kafka Consumer...");
            this.thread = new Thread(this);
            this.thread.start();
        }
    }

    @Override
    public void run() {
        if (fieryConfig.getKafkaenable()) {
            KafkaConsumer<String, String> consumer = KafkaUtil.getConsumer(fieryConfig.getKafkaserver(), fieryConfig.getKafkagroupid());
            consumer.subscribe(Arrays.asList(fieryConfig.getKafkatopic().split(",")));

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(1000);

                for (ConsumerRecord<String, String> record : records) {
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
        }
    }

    @Override
    public void destroy() {

    }

}
