package org.weiboad.ragnar.server.util;

import java.lang.management.ManagementFactory;

public class ProcessHelper {
    public String getPid() {
        // get name representing the running Java virtual machine.
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.split("@")[0];
        if (pid.trim().length() > 0) {
            return pid;
        } else {
            return "-1";
        }
    }
}
