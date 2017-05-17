package com.ragnar.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

public class RPCIDKeySortComparator implements Comparator<String> {
    Logger log = LoggerFactory.getLogger(RPCIDKeySortComparator.class);

    @Override
    public int compare(String key1, String key2) {
        String[] key1Arr = key1.split("\\.");
        String[] key2Arr = key2.split("\\.");

        if (key1Arr.length > key2Arr.length) {
            //log.info(">| first:" + key1 + " second:" + key2 + "");

            for (int keyindex = 0; keyindex < key1Arr.length; keyindex++) {

                int key1int;
                int key2int;

                key1int = Integer.parseInt(key1Arr[keyindex]);

                //fill zero
                if (keyindex >= key2Arr.length) {
                    key2int = 0;
                } else {
                    key2int = Integer.parseInt(key2Arr[keyindex]);
                }


                if (key1int > key2int) {
                    //log.info(" >> first:" + key1 + " second:" + key2 + "");
                    return 1;
                }

                if (key1int < key2int) {
                    //log.info(" >< first:" + key1 + " second:" + key2 + "");
                    return -1;
                }
                //same
                if (key1int == key2int) {
                    //log.info(" >= first:" + key1 + " second:" + key2 + "");

                    continue;
                }
            }
            return 1;
        }

        if (key1Arr.length < key2Arr.length) {
            //log.info("<| first:" + key1 + " second:" + key2 + "");

            for (int keyindex = 0; keyindex < key1Arr.length; keyindex++) {

                int key1int;
                int key2int;

                key2int = Integer.parseInt(key1Arr[keyindex]);

                //fill zero
                if (keyindex >= key1Arr.length) {
                    key1int = 0;
                } else {
                    key1int = Integer.parseInt(key1Arr[keyindex]);
                }

                if (key1int > key2int) {
                    //log.info(" <> first:" + key1 + " second:" + key2 + "");
                    return -1;
                }

                if (key1int < key2int) {
                    //log.info(" << first:" + key1 + " second:" + key2 + "");
                    return 1;
                }
                //same
                if (key1int == key2int) {
                    //log.info(" <= first:" + key1 + " second:" + key2 + "");
                    continue;
                }
            }
            return -1;
        }

        if (key1Arr.length == key2Arr.length) {
            //log.info("=| first:" + key1 + " second:" + key2 + "");

            for (int keyindex = 0; keyindex < key1Arr.length; keyindex++) {

                int key1int = Integer.parseInt(key1Arr[keyindex]);
                int key2int = Integer.parseInt(key2Arr[keyindex]);

                if (key1int > key2int) {
                    //log.info(" => first:" + key1 + " second:" + key2 + "");
                    return 1;
                }

                if (key1int < key2int) {
                    //log.info(" =< first:" + key1 + " second:" + key2 + "");
                    return -1;
                }
                //same
                if (key1int == key2int) {
                    //log.info(" == first:" + key1 + " second:" + key2 + "");
                    continue;
                }
            }
            return 0;
        }
        return 0;
    }
}
