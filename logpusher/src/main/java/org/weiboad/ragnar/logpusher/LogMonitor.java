package org.weiboad.ragnar.logpusher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weiboad.ragnar.util.DateTimeHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogMonitor {

    private Logger log = LoggerFactory.getLogger(LogMonitor.class);
    private Map<String, Long> fileInfoMap = new HashMap<>();
    private Map<String, File> fileMap = new HashMap<>();
    private Map<String, BufferedReader> bufferReaderMap = new HashMap<>();

    private ConcurrentLinkedQueue<String> sendBizLogQueue = new ConcurrentLinkedQueue<String>();
    private ConcurrentLinkedQueue<String> sendMetaLogQueue = new ConcurrentLinkedQueue<String>();

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

                //found new file
                if (fileinfo.isFile() && !fileInfoMap.containsKey(filepath)) {
                    log.info("New File:" + filepath);
                    fileInfoMap.put(filepath, 0L);
                    fileMap.put(filepath, file);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    private void cleanupOldFileInfoList(Integer outime) {
        if (outime <= 0) {
            return;
        }

        ArrayList<String> cleanUpList = new ArrayList<>();
        for (Map.Entry<String, Long> ent : fileInfoMap.entrySet()) {
            //log.info(ent.getKey() + ":" + ent.getValue());

            File file = new File(ent.getKey());
            if (!file.exists()) {
                cleanUpList.add(ent.getKey());
                continue;
            }

            //expire file
            if (file.lastModified() / 1000 < DateTimeHelper.getCurrentTime() - outime * 86400) {
                //removed
                file.delete();
                cleanUpList.add(ent.getKey());
            }
        }

        //clean up the list
        for (String filePath : cleanUpList) {

            log.info("file remove:" + filePath);

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

        //limit the length
        //maxProcessData * 20
        if (sendBizLogQueue.size() > 20) {
            return;
        }

        //loop the file list
        for (Map.Entry<String, Long> ent : fileInfoMap.entrySet()) {
            String filePath = ent.getKey();
            //limit the length
            //maxProcessData * 20
            if (sendBizLogQueue.size() > 20) {
                break;
            }

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

                    //limit the length
                    //maxProcessData * 20
                    if (sendBizLogQueue.size() > 20) {
                        break;
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

            // insert to queue
            if (!combinedContent.substring(0, 1).equals("[")) {
                sendMetaLogQueue.add(combinedContent.toString());
            } else {
                sendBizLogQueue.add(combinedContent.toString());
            }


        }//file loop
    }

    public void start(String path, String host, Integer outtime, Integer threadcount) {

        if (path.isEmpty() || host.isEmpty()) {
            log.error("parameter:-path or -host was not set!");
            return;
        }

        //curl Thread Pool
        CurlThreadPool curlThreadPool = new CurlThreadPool(host, sendBizLogQueue, sendMetaLogQueue, threadcount);
        curlThreadPool.start();

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
