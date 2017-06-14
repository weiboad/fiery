package org.weiboad.ragnar.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "fiery")
public class FieryConfig {
    private int keepdataday;
    private String dbpath;
    private String indexpath;

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
