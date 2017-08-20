package org.weiboad.ragnar.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "fiery")
public class FieryConfig {

    private int keepdataday;

    private String dbpath;

    private String indexpath;

    private Boolean kafkaenable;

    private String kafkaserver;

    private String kafkagroupid;

    private String mailfrom;

    public String getMailfrom() {
        return mailfrom;
    }

    public void setMailfrom(String mailfrom) {
        this.mailfrom = mailfrom;
    }

    public String getMailto() {
        return mailto;
    }

    public void setMailto(String mailto) {
        this.mailto = mailto;
    }

    private String mailto;

    public String getKafkagroupid() {
        return kafkagroupid;
    }

    public void setKafkagroupid(String kafkagroupid) {
        this.kafkagroupid = kafkagroupid;
    }

    public Boolean getKafkaenable() {
        return kafkaenable;
    }

    public void setKafkaenable(Boolean kafkaenable) {
        this.kafkaenable = kafkaenable;
    }

    public String getKafkaserver() {
        return kafkaserver;
    }

    public void setKafkaserver(String kafkaserver) {
        this.kafkaserver = kafkaserver;
    }

    public String getKafkatopic() {
        return kafkatopic;
    }

    public void setKafkatopic(String kafkatopic) {
        this.kafkatopic = kafkatopic;
    }

    private String kafkatopic;

    public String getDbpath() {
        return dbpath;
    }

    public void setDbpath(String dbpath) {
        this.dbpath = dbpath;
    }

    public String getIndexpath() {
        return indexpath;
    }

    public void setIndexpath(String indexpath) {
        this.indexpath = indexpath;
    }

    public int getKeepdataday() {
        return keepdataday;
    }

    public void setKeepdataday(int keepdataday) {
        this.keepdataday = keepdataday;
    }
}
