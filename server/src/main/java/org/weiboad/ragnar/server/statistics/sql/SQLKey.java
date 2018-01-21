package org.weiboad.ragnar.server.statistics.sql;

import org.weiboad.ragnar.server.util.SimHash;

public class SQLKey {
    private SimHash hash;

    private String sql;

    private String pureSql;

    public SimHash getHash() {
        return hash;
    }

    public void setHash(SimHash hash) {
        this.hash = hash;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getPureSql() {
        return pureSql;
    }

    public void setPureSql(String pureSql) {
        this.pureSql = pureSql;
    }
}
