package logpusher;

import com.ragnar.server.util.DateTimeHepler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
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
                        if (modTime > DateTimeHepler.getCurrentTime() - 60 * 60) {
                            fetchFileAppendContent(filepath, modTime);
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }
    }
/*
    private void pushBizLogToServer(String url) {
        long pushcount = 0;
        String postData = "";

        while (sendBizLogQueue.peek() != null) {
            postData += (sendBizLogQueue.poll() + "\n");
            pushcount++;
            if (pushcount > this.processMaxCount) {
                break;
            }
        }
        if (postData.length() > 0) {
            String retString = postHttp(url, postData);
            if (retString == "") {
                return;
            }
            try {
                JsonParser jsonParser = new JsonParser();
                JsonObject retObj = jsonParser.parse(retString).getAsJsonObject();
                if (retObj.get("code").getAsInt() != 0) {
                    log.error("bizlog:" + retString);
                } else {
                    log.info("bizlog:" + retString);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
    }
    private boolean pushMetaLogToServer(String url) {
        long pushcount = 0;

        String data = "";
        while (sendMetaLogQueue.peek() != null) {
            data += (sendMetaLogQueue.poll() + "\n");
            pushcount++;
            if (pushcount > this.processMaxCount) {
                break;
            }
        }
        if (data.equals("")) {
            return true;
        }
        if (data.length() > 0) {
            String retString = postHttp(url, data);
            if (retString == "") {
                return false;
            }
            try {
                JsonParser jsonParser = new JsonParser();
                JsonObject retObj = jsonParser.parse(retString).getAsJsonObject();
                if (retObj.get("code").getAsInt() != 0) {
                    log.error("metalog:" + retString);
                    return false;
                } else {
                    log.info("metalog:" + retString);
                    return true;
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }
        return true;
    }

    private String postHttp(String url, String postData) {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "stdout");
        String responseMsg = "";
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setContentCharset("UTF-8");
        PostMethod postMethod = new PostMethod(url);
        postMethod.addParameter("contents", postData);
        try {
            httpClient.executeMethod(postMethod);
            responseMsg = postMethod.getResponseBodyAsString();
        } catch (HttpException e) {
            log.debug(e.getMessage());
        } catch (IOException e) {
            log.debug(e.getMessage());
        } finally {
            postMethod.releaseConnection();
        }
        return responseMsg;
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Ragnar Fiery LogPusher");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print("contents=" + URLEncoder.encode(postData, "utf-8"));
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            log.error("Post Error:" + e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
*/

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
            if (file.lastModified() / 1000 < DateTimeHepler.getCurrentTime() - outtimeInt * 86400) {
                if (file.delete()) {
                    dellist.add(ent.getKey());
                }
            }
        }
        for (String fileinfokey : dellist) {
            fileInfo.remove(fileinfokey);
        }
    }

    public void start(String path, String host, String outtime,Integer threadcount) {

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

            //pushBizLogToServer("http://" + host + "/ragnar/log/bizlog/put");
            //pushMetaLogToServer("http://" + host + "/ragnar/log/metalog/put");
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                //log.error(e.getMessage());
            }
        }

    }
}
