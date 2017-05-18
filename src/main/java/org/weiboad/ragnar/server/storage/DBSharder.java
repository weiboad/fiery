package org.weiboad.ragnar.server.storage;

import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBSharder {

    private RocksDB db;

    private Options options;

    private Env env;

    private Logger log;

    public DBSharder(String dbpath, Long timestamp) throws RocksDBException {

        log = LoggerFactory.getLogger(DBSharder.class);

        RocksDB.loadLibrary();

        options = new Options();

        env = options.getEnv();

        env.setBackgroundThreads(2);

        options.setEnv(env);

        options.setCreateIfMissing(true);

        options.setDbLogDir(dbpath + "/logs/");

        options.setMergeOperator(new StringAppendOperator());

        db = RocksDB.open(options, dbpath + "/" + timestamp);
    }

    public boolean put(String key, String val) {

        if (key.length() == 0) return false;

        try {
            db.put(key.getBytes(), val.getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return false;
    }

    public boolean merge(String key, String val) {

        if (key.length() == 0) return false;

        try {
            db.merge(key.getBytes(), val.getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return false;
    }

    public boolean del(String key) {

        if (key.length() == 0) return false;

        try {
            db.remove(key.getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return false;
    }

    public String get(String key) {

        if (key.length() == 0) return null;

        try {
            byte[] result = db.get(key.getBytes());
            if (result != null && result.length > 0) {
                String resultString = new String(result);
                return resultString;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return null;
    }


    public void close() {

        if (db != null) db.close();
        //options.dispose();
    }
}
