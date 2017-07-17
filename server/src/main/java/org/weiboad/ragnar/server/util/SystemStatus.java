package org.weiboad.ragnar.server.util;

public class SystemStatus {
    public long getMemoryMax() {
        return Runtime.getRuntime().maxMemory();
    }

    public long getMemoryTotal() {
        return Runtime.getRuntime().totalMemory();
    }

    public long getMemoryFree() {
        return Runtime.getRuntime().freeMemory();
    }

    public long getMemoryUsed() {
        return getMemoryTotal() - getMemoryFree();
    }
}
