package com.ragnar.server.storage;

import org.rocksdb.MergeOperator;

/**
 * Java的Rocksdb暂时不支持这个……
 */
public class DBMergerOverrider implements MergeOperator {
    @Override
    public long newMergeOperatorHandle() {
        return 0;
    }
}
