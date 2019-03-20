package org.weiboad.ragnar.logpusher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weiboad.ragnar.http.CurlThreadPool;
import org.weiboad.ragnar.kafka.ProviderThread;
import org.weiboad.ragnar.util.DateTimeHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class LogMonitor {

    private Logger log = LoggerFactory.getLogger(LogMonitor.class);
    private Map<String, Long> fileInfoMap = new ConcurrentHashMap<>();
    private Map<String, File> fileMap = new ConcurrentHashMap<>();
    private Map<String, BufferedReader> bufferReaderMap = new ConcurrentHashMap<>();

    private BlockingQueue<String> sendBizLogQueue = new LinkedBlockingQueue<>(5000);
    private BlockingQueue<String> sendMetaLogQueue = new LinkedBlockingQueue<String>(5000);

    //max load log Data
    private int maxProcessData = 1048576;//max load one file

    /**
     * scan the new file
     *
     * @param path
     */
    private void scanTheFolderFileList(String path) {
        File file = new File(path);
        File[] tempList = file.listFiles();

        if (tempList == null) {
            return;
        }

        for (File fileinfo : tempList) {
            try {
                String filepath = fileinfo.getCanonicalPath();
                if (fileinfo.isDirectory()) {
                    //deep
                    scanTheFolderFileList(filepath);
                    continue;
                }

                //old file will not scaned
                if (fileinfo.lastModified() / 1000 < DateTimeHelper.getCurrentTime() - (7 * 24 * 3600)) {
                    continue;
                }

                //found new file
                if (fileinfo.isFile() && !fileInfoMap.containsKey(filepath)) {
                    log.info("New File:" + filepath);
                    fileInfoMap.put(filepath, 0L);
                    fileMap.put(filepath, file);
                    continue;
                }

                //check the file is delete and renew?
                //if the length more than the current len
                //the file must be renew
                if (fileInfoMap.containsKey(filepath) && fileInfoMap.get(filepath) > fileinfo.length() + 10) {
                    log.info("renew the File:" + filepath + " offset:" + fileInfoMap.get(filepath) + " length:" + fileinfo.length());

                    try {
                        bufferReaderMap.get(filepath).close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //clean up
                    bufferReaderMap.remove(filepath);
                    fileMap.remove(filepath);

                    //create again
                    fileInfoMap.put(filepath, 0L);
                    fileMap.put(filepath, file);
                }

            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    private void cleanupOldFileInfoList(Integer outime) {

        ArrayList<String> cleanUpList = new ArrayList<>();
        for (Map.Entry<String, Long> ent : fileInfoMap.entrySet()) {
            //log.info(ent.getKey() + ":" + ent.getValue());

            File file = new File(ent.getKey());
            if (!file.exists()) {
                cleanUpList.add(ent.getKey());
                continue;
            }

            //expire file
            if (outime > 0 && file.lastModified() / 1000 < DateTimeHelper.getCurrentTime() - outime * 86400) {
                //removed
                boolean ret = file.delete();
                log.info("file fd remove:" + ent.getKey() + " result:" + ret);
                if(ret){
                    cleanUpList.add(ent.getKey());
                }
            }
        }

        //clean up the list
        for (String filePath : cleanUpList) {

            log.info("file map remove:" + filePath + "");

            fileInfoMap.remove(filePath);

            if (bufferReaderMap.containsKey(filePath)) {
                BufferedReader bfr = bufferReaderMap.get(filePath);
                bufferReaderMap.remove(filePath);
                try {
                    bfr.close();
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }

        }
    }

    private void fetchTheFileAppend() {

        //loop the file list
        for (Map.Entry<String, Long> ent : fileInfoMap.entrySet()) {
            String filePath = ent.getKey();

            //make sure the file opened
            if (!bufferReaderMap.containsKey(filePath)) {
                log.info("opend file:" + filePath);
                File file = new File(filePath);

                //not found
                if (!file.exists()) {
                    fileMap.remove(filePath);
                    fileInfoMap.remove(filePath);
                    log.error("Not Found:" + filePath);
                    continue;
                }

                BufferedReader reader;
                try {
                    reader = new BufferedReader(new FileReader(file));
                    bufferReaderMap.put(filePath, reader);
                } catch (Exception e) {
                    log.error("buffer reader create fail:" + e.getMessage() + " | " + filePath);
                    continue;
                }
            }

            String tempString = "";
            //combined result
            StringBuilder combinedContent = new StringBuilder();
            //processed Data Total
            Long processLength = 0L;
            Long offset = fileInfoMap.get(filePath);

            BufferedReader fileReader = bufferReaderMap.get(filePath);
            try {
                while ((tempString = fileReader.readLine()) != null) {
                    offset += (tempString.length() + 1);
                    fileInfoMap.put(filePath, offset);

                    //processed data len total
                    processLength += (tempString.length() + 1);

                    //combine the content
                    if (tempString.trim().length() > 0) {
                        combinedContent.append(tempString + "\n");
                    }

                    //ok process more than
                    if (processLength > maxProcessData) {
                        break;
                    }
                }
            } catch (Exception e) {
                log.error(filePath + " read fail:" + e.getMessage());
                // when the Exception
                // clean up all
                fileInfoMap.remove(filePath);
                fileMap.remove(filePath);
                try {
                    bufferReaderMap.get(filePath).close();
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
                bufferReaderMap.remove(filePath);
            }

            //nothing ? ignore
            if (combinedContent.length() == 0) {
                continue;
            }

            try {
                // insert to queue
                if (!combinedContent.substring(0, 1).equals("[")) {
                    sendMetaLogQueue.put(combinedContent.toString());
                } else {
                    sendBizLogQueue.put(combinedContent.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }//file loop
    }

    public void startHttpPush(String host, Integer threadcount) {
        if (host.isEmpty()) {
            log.error("parameter: -host was not set!");
            return;
        }

        //curl Thread Pool
        CurlThreadPool curlThreadPool = new CurlThreadPool(host, sendBizLogQueue, sendMetaLogQueue, threadcount);
        curlThreadPool.start();
    }

    public void startKafkaPush(String KafkaTopic, String ServerList) {
        if (KafkaTopic.length() == 0 || ServerList.length() == 0) {
            log.error("parameter: -kafkatopic -kafkaserver was not set!");
            return;
        }

        ProviderThread providerThread = new ProviderThread(KafkaTopic, ServerList, sendMetaLogQueue, sendBizLogQueue);
        providerThread.start();
    }

    public void startFileScan(String path, Integer outtime) {

        if (path.isEmpty()) {
            log.error("parameter:-path was not set!");
            return;
        }


        while (true) {
            scanTheFolderFileList(path);
            cleanupOldFileInfoList(outtime);
            fetchTheFileAppend();

            try {
                Thread.sleep(50);
            } catch (Exception e) {
                //log.error(e.getMessage());
            }
        }

    }
    //waticher
}
