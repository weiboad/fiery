package org.weiboad.ragnar.logpusher;

public class LogPusherConfig {

    private String path = "./";

    private int outTime = 0;

    private String pushType = "http";

    private String kafkaServer = "";

    private String kafkaTopic = "";

    private String host = "127.0.0.1:9090";

    private int threadCount = 10;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getOutTime() {
        return outTime;
    }

    public void setOutTime(int outTime) {
        this.outTime = outTime;
    }

    public String getPushType() {
        return pushType;
    }

    public void setPushType(String pushType) {
        this.pushType = pushType;
    }

    public String getKafkaServer() {
        return kafkaServer;
    }

    public void setKafkaServer(String kafkaServer) {
        this.kafkaServer = kafkaServer;
    }

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public void setKafkaTopic(String kafkaTopic) {
        this.kafkaTopic = kafkaTopic;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }
}
