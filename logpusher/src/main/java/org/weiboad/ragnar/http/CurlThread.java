package org.weiboad.ragnar.http;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import okhttp3.OkHttpClient.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weiboad.ragnar.util.DateTimeHelper;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class CurlThread extends Thread {

    private Logger log = LoggerFactory.getLogger(CurlThread.class);

    private String host;

    private ConcurrentLinkedQueue<String> sendBizLogQueue;

    private ConcurrentLinkedQueue<String> sendMetaLogQueue;

    private int processMaxCount = 1000000;//9M


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
        String result = "";

        Builder builder = new Builder();
        builder.connectTimeout(10000, TimeUnit.MILLISECONDS);
        builder.readTimeout(10000,TimeUnit.MILLISECONDS);
        builder.writeTimeout(10000,TimeUnit.MILLISECONDS);
        builder.followRedirects(true);
        builder.retryOnConnectionFailure(false);
        OkHttpClient client = builder.build();

        //MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
        RequestBody body =  new FormBody.Builder().add("contents", postData).build();
        //RequestBody.create(mediaType, "contents=" + postData);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            result = response.body().string();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

        /*
        String result = "";

        if (postData.trim().length() == 0) {
            return "";
        }
        HttpPost httppost = new HttpPost(url);
        CloseableHttpClient httpClient = HttpClients.createDefault();

        //header
        httppost.addHeader("connection", "close");
        httppost.addHeader("user-agent", "Ragnar Fiery LogPusher");
        httppost.addHeader("Content-Type", "application/x-www-form-urlencoded");

        //Configure
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(10000)
                .setConnectTimeout(10000)
                .setConnectionRequestTimeout(20000)
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

        httppost.releaseConnection();
        return result;
        */
    }

    private String fetchQueue(ConcurrentLinkedQueue<String> queue, int maxtime) {
        StringBuilder resultString = new StringBuilder();
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
                }
            } else {
                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                    //log.error(e.getMessage());
                }
            }

            if ((DateTimeHelper.getCurrentTime() - startTime) > maxtime || resultString.length() > processMaxCount) {
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
