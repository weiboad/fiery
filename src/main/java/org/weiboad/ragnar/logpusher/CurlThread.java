package org.weiboad.ragnar.logpusher;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CurlThread extends Thread {

    private Logger log = LoggerFactory.getLogger(CurlThread.class);

    private String host;

    private ConcurrentLinkedQueue<String> sendBizLogQueue;

    private ConcurrentLinkedQueue<String> sendMetaLogQueue;

    private int processMaxCount = 1000;


    public CurlThread(String host, ConcurrentLinkedQueue<String> sendBizLogQueue, ConcurrentLinkedQueue<String> sendMetaLogQueue) {
        this.host = host;
        this.sendBizLogQueue = sendBizLogQueue;
        this.sendMetaLogQueue = sendMetaLogQueue;
    }

    public void run() {
        while (true) {
            //biz log
            pushBizLogToServer("http://" + host + "/ragnar/log/bizlog/put");
            //meta log
            pushMetaLogToServer("http://" + host + "/ragnar/log/metalog/put");
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                //log.error(e.getMessage());
            }
        }
    }

    private String postHttp(String url, String postData) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";

        if (postData.trim().length() == 0) {
            return "";
        }

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
        } finally {
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

    private void pushBizLogToServer(String url) {
        long pushcount = 0;
        String postData = "";
        String content = "";

        while ((content = sendBizLogQueue.poll()) != null) {

            if (content.trim().length() > 0) {
                postData += (content.trim() + "\n");
                pushcount++;
            }
            if (pushcount > this.processMaxCount) {
                break;
            }
        }
        if (postData.trim().length() > 0) {
            String retString = postHttp(url, postData);
            if (retString.equals("")) {
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
        String content = "";

        String data = "";
        while ((content = sendBizLogQueue.poll()) != null) {

            if (content.trim().length() > 0) {
                data += (content.trim() + "\n");
                pushcount++;
            }
            if (pushcount > this.processMaxCount) {
                break;
            }
        }
        if (data.equals("")) {
            return true;
        }
        if (data.trim().length() > 0) {
            String retString = postHttp(url, data);
            if (retString.equals("")) {
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
}
