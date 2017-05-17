package com.ragnar.server.util;

import java.util.HashMap;
import java.util.Map;

public class TraceIdDecoder {

    public static byte[] longToByte(long number) {
        long temp = number;
        byte[] b = new byte[8];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Long(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }

    public static Map<String, String> decodev12(String traceid) {
        Map<String, String> result = new HashMap<>();

        long TraceIdLong = Long.parseLong(WeiboMidHelper.Mid2Uid(traceid));
        //LoggerFactory.getLogger(TraceIdDecoder.class).debug("tracedecode:" + TraceIdLong);

        long iplong = ((TraceIdLong >> 46) & 0x000000000000ffff);
        long timestamp = ((TraceIdLong >> 18) & 0x000000000fffffff) + 1483200000;
        long ms = ((TraceIdLong >> 8) & 0x00000000000003ff);

        result.put("traceid", traceid);

        result.put("idc", "" + ((TraceIdLong >> 62) & 0x0000000000000003));

        result.put("iplong", iplong + "");

        result.put("ip", (iplong / 256) + "." + iplong % 256);

        result.put("time", "" + timestamp);

        result.put("ms", "" + ms);

        result.put("timestamp", timestamp + "." + ms);

        result.put("rand", "" + (TraceIdLong & 0x00000000000000ff));

        return result;

    }


    public static Map<String, String> decodev10(String traceid) {
        Map<String, String> result = new HashMap<>();

        long TraceIdLong = Long.parseLong(WeiboMidHelper.Mid2Uid(traceid));

        long iplong = ((TraceIdLong >> 46) & 0x000000000000ffff);
        long timestamp = ((TraceIdLong >> 18) & 0x000000000fffffff) + 1483200000;
        long ms = ((TraceIdLong >> 4) & 0x0000000000003fff);

        result.put("traceid", traceid);

        result.put("idc", "" + ((TraceIdLong >> 62) & 0x0000000000000003));

        result.put("iplong", iplong + "");

        result.put("ip", (iplong / 256) + "." + iplong % 256);

        result.put("time", "" + timestamp);

        result.put("ms", "" + ms);

        result.put("timestamp", timestamp + "." + ms);

        result.put("rand", "" + (TraceIdLong & 0x00000000000000ff));

        return result;

    }

}
