package org.weiboad.ragnar.logpusher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weiboad.ragnar.server.util.DateTimeHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogPusherMain {
    private Logger log = LoggerFactory.getLogger(LogPusherMain.class);
    private Map<String, Map<String, Long>> fileInfo = new HashMap<String, Map<String, Long>>();
    private ConcurrentLinkedQueue<String> sendBizLogQueue = new ConcurrentLinkedQueue<String>();
    private ConcurrentLinkedQueue<String> sendMetaLogQueue = new ConcurrentLinkedQueue<String>();

    private int processMaxCount = 1000;

    private void fetchFileAppendContent(String filepath, long modTime) {
        if (sendBizLogQueue.size() > 2000 || sendMetaLogQueue.size() > 2000) {
            return;
        }
        File file = new File(filepath);
        //not found
        if (!file.exists()) {
            fileInfo.remove(filepath);
            log.info("=== File Not Found ... " + filepath);
            return;
        }

        //get the file size
        long fileSize = file.length();
        long offset = 0;
        //have record
        if (fileInfo.containsKey(filepath)) {
            offset = fileInfo.get(filepath).get("offset");
            fileInfo.get(filepath).put("lastupdate", modTime);
        } else {
            //log.debug("=== Found New File === " + filepath);
            Map<String, Long> fileData = new HashMap<String, Long>();
            fileData.put("offset", offset);
            fileData.put("lastupdate", modTime);
            fileData.put("size", fileSize);
            fileInfo.put(filepath, fileData);
        }

        //file have been move or empty
        if (offset > fileSize) {
            offset = 0;
        }

        long processedCount = 0;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            reader.skip(offset);
            while ((tempString = reader.readLine()) != null) {
                if (tempString.length() == 0) {
                    offset += 1;
                    continue;
                }
                if (processedCount >= this.processMaxCount) {
                    break;
                }
                offset += (tempString.length() + 1);
                if (!tempString.substring(0, 1).equals("[")) {
                    sendMetaLogQueue.add(tempString);
                } else {
                    sendBizLogQueue.add(tempString);
                }
                processedCount++;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        fileInfo.get(filepath).put("offset", offset);
        fileInfo.get(filepath).put("size", fileSize);
        //log.info("fileinfo:path="+filepath+",offset="+offset+",size="+fileSize);
    }

    /**
     * scan the folder the new update file
     *
     * @param path
     */
    private void scanRecentUpdateFile(String path) {
        File file = new File(path);
        File[] tempList = file.listFiles();
        if (tempList != null) {
            for (File fileinfo : tempList) {
                try {
                    String filepath = fileinfo.getCanonicalPath();
                    if (!fileinfo.isFile() && fileinfo.isDirectory()) {
                        scanRecentUpdateFile(filepath);
                    } else if (fileinfo.isFile()) {
                        long modTime = fileinfo.lastModified() / 1000;
                        if (modTime > DateTimeHelper.getCurrentTime() - 60 * 60) {
                            fetchFileAppendContent(filepath, modTime);
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    private void cleanupOldFileInfoList(String outime) {
        if (outime.isEmpty() || outime.equals("")) {
            return;
        }

        Integer outtimeInt;
        try {
            outtimeInt = Integer.valueOf(outime);
        } catch (Exception e) {
            outtimeInt = 7;
            log.error(e.getMessage());
        }

        ArrayList<String> dellist = new ArrayList<>();
        for (Map.Entry<String, Map<String, Long>> ent : fileInfo.entrySet()) {
            File file = new File(ent.getKey());
            if (!file.exists()) {
                dellist.add(ent.getKey());
                continue;
            }
            //判断过期时间是否存在，如果存在删除过期的文件
            if (file.lastModified() / 1000 < DateTimeHelper.getCurrentTime() - outtimeInt * 86400) {
                if (file.delete()) {
                    dellist.add(ent.getKey());
                }
            }
        }
        for (String fileinfokey : dellist) {
            fileInfo.remove(fileinfokey);
        }
    }

    public void start(String path, String host, String outtime, Integer threadcount) {

        if (path.isEmpty() || host.isEmpty()) {
            log.error("parameter:-path or -host was not set!");
            return;
        }
        //pull thread
        CurlThreadPool curlThreadPool = new CurlThreadPool(host, sendBizLogQueue, sendMetaLogQueue, threadcount);
        curlThreadPool.start();

        while (true) {
            scanRecentUpdateFile(path);
            cleanupOldFileInfoList(outtime);

            try {
                Thread.sleep(10);
            } catch (Exception e) {
                //log.error(e.getMessage());
            }
        }

    }
}
