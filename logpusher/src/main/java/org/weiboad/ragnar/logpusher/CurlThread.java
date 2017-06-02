package org.weiboad.ragnar.logpusher;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weiboad.ragnar.util.DateTimeHelper;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CurlThread extends Thread {

    private Logger log = LoggerFactory.getLogger(CurlThread.class);

    private String host;

    private ConcurrentLinkedQueue<String> sendBizLogQueue;

    private ConcurrentLinkedQueue<String> sendMetaLogQueue;

    private int processMaxCount = 2000000;//19M


    public CurlThread(String host, ConcurrentLinkedQueue<String> sendBizLogQueue, ConcurrentLinkedQueue<String> sendMetaLogQueue) {
        this.host = host;
        this.sendBizLogQueue = sendBizLogQueue;
        this.sendMetaLogQueue = sendMetaLogQueue;
    }

    public void run() {
        while (true) {
            //biz log
            boolean retBiz = pushBizLogToServer("http://" + host + "/ragnar/log/bizlog/put");
            //meta log
            boolean retMeta = pushMetaLogToServer("http://" + host + "/ragnar/log/metalog/put");

            if (!retBiz && !retMeta) {
                try {
                    Thread.sleep(20);
                } catch (Exception e) {
                    //log.error(e.getMessage());
                }
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
        HttpPost httppost = new HttpPost(url);
        CloseableHttpClient httpClient = HttpClients.createDefault();

        //header
        httppost.addHeader("connection", "Keep-Alive");
        httppost.addHeader("user-agent", "Ragnar Fiery LogPusher");
        httppost.addHeader("Content-Type", "application/x-www-form-urlencoded");

        //Configure
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(4000)
                .setConnectTimeout(4000)
                .setConnectionRequestTimeout(10000)
                .setContentCompressionEnabled(true)
                .setExpectContinueEnabled(true)
                .setMaxRedirects(3)
                .setRedirectsEnabled(true)
                .build();
        httppost.setConfig(requestConfig);

        //set parameter
        List<NameValuePair> formparams = new ArrayList<>();
        formparams.add(new BasicNameValuePair("contents", postData));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
        httppost.setEntity(entity);

        try {
            CloseableHttpResponse response = httpClient.execute(httppost);
            result = EntityUtils.toString(response.getEntity());

        } catch (Exception e) {
            e.printStackTrace();
            log.error("http post error:" + e.getMessage());
        }

        return result;
    }

    private String fetchQueue(ConcurrentLinkedQueue<String> queue, int maxtime) {
        StringBuffer resultString = new StringBuffer();
        int collectCount = 0;
        Long startTime = DateTimeHelper.getCurrentTime();

        if (queue.peek() == null) {
            return "";
        }

        String queueString = "";
        while (true) {
            queueString = queue.poll();
            if (queueString != null) {
                if (queueString.trim().length() > 0) {
                    resultString.append(queueString + "\n");
                    collectCount += queueString.length();
                }
            } else {
                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                    //log.error(e.getMessage());
                }
            }

            if ((DateTimeHelper.getCurrentTime() - startTime) > maxtime || collectCount > processMaxCount) {
                break;
            }
        }

        return resultString.toString();
    }

    private boolean pushBizLogToServer(String url) {
        String postData = fetchQueue(sendBizLogQueue, 2);

        if (postData.trim().length() > 0) {
            log.info("fetch biz size:" + postData.trim().length());

            String retString = postHttp(url, postData);
            if (retString.equals("")) {
                return false;
            }
            try {
                JsonParser jsonParser = new JsonParser();
                JsonObject retObj = jsonParser.parse(retString).getAsJsonObject();
                if (retObj.get("code").getAsInt() != 0) {
                    log.error("bizlog:" + retString);
                } else {
                    log.info("bizlog:" + retString);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
        return false;
    }

    private boolean pushMetaLogToServer(String url) {

        String data = fetchQueue(sendMetaLogQueue, 2);

        if (data.trim().length() > 0) {
            log.info("fetch meta size:" + data.trim().length());

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
        return false;
    }
}
