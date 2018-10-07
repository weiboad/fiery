package org.weiboad.ragnar.server.statistics.error;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.weiboad.ragnar.server.config.FieryConfig;
import org.weiboad.ragnar.server.util.DateTimeHelper;
import org.weiboad.ragnar.server.util.MailHelper;
import org.weiboad.ragnar.server.util.SimHash;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Scope("singleton")
public class ErrorStatistic {

    Logger log = LoggerFactory.getLogger(ErrorStatistic.class);

    private Map<Long, Map<String, ErrorStatisticStruct>> _alarmLogMap = new ConcurrentHashMap<>();

    private Map<Long, Map<String, ErrorStatisticStruct>> _errorLogMap = new ConcurrentHashMap<>();

    private Map<Long, Map<String, ErrorStatisticStruct>> _exceptionLogMap = new ConcurrentHashMap<>();

    @Autowired
    FieryConfig fieryConfig;

    @Autowired
    MailHelper mailHelper;

    public void addAlarmLogMap(int type, String str, Long timestamp) {
        if (str == null) {
            return;
        }
        JsonParser jsonToken = new JsonParser();
        JsonObject objToken = jsonToken.parse(str).getAsJsonObject();
        String filePath = objToken.get("filepath").getAsString();
        Integer line = objToken.get("fileline").getAsInt();
        objToken.remove("mytraceid");
        objToken.remove("myrpcid");
        objToken.remove("filepath");
        objToken.remove("fileline");
        String tokenStr = objToken.toString();
        String token = getToken(tokenStr);
        SimHash hash1;
        try {
            hash1 = new SimHash(token, 64);
            //hash1.subByDistance(hash1,3);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return;
        }
        switch (type) {
            case 5:
                addLogInfo(_errorLogMap, str, token, timestamp, filePath, line, hash1, 5);
                break;
            case 6:
                addLogInfo(_alarmLogMap, str, token, timestamp, filePath, line, hash1, 6);
                break;
            case 7:
                addLogInfo(_exceptionLogMap, str, token, timestamp, filePath, line, hash1, 7);
                break;
        }
    }

    private String getToken(String tokenStr) {
        StringBuffer token = new StringBuffer();
        //特殊符号替换空格
        for (int i = 0; i < tokenStr.length(); i++) {
            char c = tokenStr.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))) {
                token.append(" ");
            } else {
                token.append(c);
            }
        }

        return token.toString().replaceAll("\\s+", " ");
    }

    private void addLogInfo(Map<Long, Map<String, ErrorStatisticStruct>> logMap, String logStr, String token, Long nowTime, String filePath, Integer line, SimHash hash, int level) {
        long dayTime = DateTimeHelper.getTimesMorning(nowTime);
        boolean issame = false;
        if (logMap.size() != 0 && logMap.get(dayTime) != null) {
            for (Map.Entry<String, ErrorStatisticStruct> entry : logMap.get(dayTime).entrySet()) {
                if (entry.getValue().getFilePath().equals(filePath) && entry.getValue().getFileLine() == line) {
                    entry.getValue().setCount(entry.getValue().getCount() + 1);
                    entry.getValue().setNewLog(logStr);
                    entry.getValue().setEndTime(nowTime);
                    issame = true;
                    break;
                }
                //int dis = entry.getKey().getDistance(entry.getKey().strSimHash,hash.strSimHash);
                //根据日志信息的长度确定海明距离的大小
                Integer simility;
                if (token.length() < 100) {
                    simility = 15;
                } else if (token.length() >= 100 && token.length() < 500) {
                    simility = 10;
                } else {
                    simility = 5;
                }
                if (entry.getValue().hash.hammingDistance(hash) <= simility) {
                    entry.getValue().addCount();
                    entry.getValue().setNewLog(logStr);
                    entry.getValue().setEndTime(nowTime);
                    issame = true;
                    break;
                }
            }
        }
        if (!issame) {
            ErrorStatisticStruct loginfo = new ErrorStatisticStruct();
            loginfo.setNewLog(logStr);
            loginfo.setCount(1);
            loginfo.setFileLine(line);
            loginfo.setFilePath(filePath);
            loginfo.setOldLog(logStr);
            loginfo.setStartTime(nowTime);
            loginfo.setEndTime(nowTime);
            loginfo.hash = hash;
            if (logMap.get(dayTime) == null) {
                Map<String, ErrorStatisticStruct> dayMap = new ConcurrentHashMap<>();
                dayMap.put(hash.getSimHash().toString(), loginfo);
                logMap.put(dayTime, dayMap);
            } else {
                logMap.get(dayTime).put(hash.getSimHash().toString(), loginfo);
            }
        }
    }

    public Map<String, Map<String, String>> getErrorData(Integer type, Long dayTime) {
        Map<Long, Map<String, ErrorStatisticStruct>> tempMap;
        Map<String, Map<String, String>> list = new Hashtable<>();
        if (type == 5) {
            tempMap = _errorLogMap;
        } else if (type == 6) {
            tempMap = _alarmLogMap;
        } else if (type == 7) {
            tempMap = _exceptionLogMap;
        } else {
            return list;
        }
        if (tempMap.size() == 0) {
            return list;
        }
        if (tempMap.get(dayTime) == null) {
            return list;
        }
        for (Map.Entry<String, ErrorStatisticStruct> ent : tempMap.get(dayTime).entrySet()) {
            Map<String, String> data = new HashMap<String, String>();
            data.put("starttime", DateTimeHelper
                    .TimeStamp2Date(String.valueOf(ent.getValue().getStartTime()), "yyyy-MM-dd HH:mm:ss"));
            data.put("endtime", DateTimeHelper
                    .TimeStamp2Date(String.valueOf(ent.getValue().getEndTime()), "yyyy-MM-dd HH:mm:ss"));
            data.put("count", ent.getValue().getCount().toString());
            JsonObject objContent = new JsonObject();
            JsonParser jsonLog = new JsonParser();
            objContent.add("firstlog", jsonLog.parse(ent.getValue().getOldLog()).getAsJsonObject());
            objContent.add("newlog", jsonLog.parse(ent.getValue().getNewLog()).getAsJsonObject());
            //objContent.addProperty("firstlog",jsonLog.parse(ent.getValue().getOldLog()).getAsJsonObject().toString());
            //objContent.addProperty("newlog",jsonLog.parse(ent.getValue().getNewLog()).getAsJsonObject().toString());
            data.put("content", objContent.toString());
            JsonParser jsonHelp = new JsonParser();
            JsonObject oldObj = (JsonObject) jsonHelp.parse(ent.getValue().getOldLog());
            String oldtraceid = oldObj.get("mytraceid").getAsString();
            String oldrpcid = oldObj.get("myrpcid").getAsString();
            data.put("oldtraceid", oldtraceid);
            data.put("oldrpcid", oldrpcid);
            JsonObject newObj = (JsonObject) jsonHelp.parse(ent.getValue().getNewLog());
            String newtraceid = newObj.get("mytraceid").getAsString();
            String newrpcid = newObj.get("myrpcid").getAsString();
            data.put("newtraceid", newtraceid);
            data.put("newrpcid", newrpcid);
            list.put(ent.getKey(), data);
        }
        return list;
    }

    public String DelLogInfo(String hashcode, Long dayTime, int type) {
        Map<Long, Map<String, ErrorStatisticStruct>> logMap;
        if (type == 5) {
            logMap = _errorLogMap;
        } else if (type == 6) {
            logMap = _alarmLogMap;
        } else if (type == 7) {
            logMap = _exceptionLogMap;
        } else {
            return "type param is error";
        }
        if (logMap.size() == 0) {
            return "daytime param is error";
        }
        if (logMap.get(dayTime) == null) {
            return "daytime param is error";
        }
        logMap.get(dayTime).remove(hashcode);
        return "delete success";
    }

    public Map<String, Integer> getAlaramStatics() {
        Map<String, Integer> result = new LinkedHashMap<>();

        for (Map.Entry<Long, Map<String, ErrorStatisticStruct>> ent : _alarmLogMap.entrySet()) {
            result.put(ent.getKey() + "", ent.getValue().size());
        }
        return result;
    }

    public Map<String, Integer> getErrorStatics() {
        Map<String, Integer> result = new LinkedHashMap<>();

        for (Map.Entry<Long, Map<String, ErrorStatisticStruct>> ent : _errorLogMap.entrySet()) {
            result.put(ent.getKey() + "", ent.getValue().size());
        }
        return result;
    }

    public Map<String, Integer> getExceptionStatics() {
        Map<String, Integer> result = new LinkedHashMap<>();

        for (Map.Entry<Long, Map<String, ErrorStatisticStruct>> ent : _exceptionLogMap.entrySet()) {
            result.put(ent.getKey() + "", ent.getValue().size());
        }
        return result;
    }

    public boolean sendMail(String subject, String content) {
        //first of this day? ok send an alarm
        if (fieryConfig.getMailfrom().length() > 0 && fieryConfig.getMailto().length() > 0) {
            try {
                mailHelper.sendSimpleMail(fieryConfig.getMailfrom(), fieryConfig.getMailto(), subject, content);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
        return false;
    }

    @Scheduled(fixedRate = 30 * 1000)
    public void DelOutTimeLog() {
        if (_errorLogMap.size() > 0) {
            ArrayList<Long> errorList = new ArrayList<>();
            for (Map.Entry<Long, Map<String, ErrorStatisticStruct>> ent : _errorLogMap.entrySet()) {
                if (ent.getKey() >= DateTimeHelper.getCurrentTime() - fieryConfig.getKeepdataday() * 86400) {
                    continue;
                }
                errorList.add(ent.getKey());
                log.info("del out time error log,log create_time:" + DateTimeHelper
                        .TimeStamp2Date(ent.getKey().toString(), "yyyy-MM-dd HH:mm:ss"));
            }
            for (Long key : errorList) {
                _errorLogMap.remove(key);
            }
        }
        if (_alarmLogMap.size() >= 0) {
            ArrayList<Long> alarmList = new ArrayList<>();
            for (Map.Entry<Long, Map<String, ErrorStatisticStruct>> ent : _alarmLogMap.entrySet()) {
                if (ent.getKey() >= DateTimeHelper.getCurrentTime() - fieryConfig.getKeepdataday() * 86400) {
                    continue;
                }
                alarmList.add(ent.getKey());
                log.info("del out time alarm log,log create_time:" + DateTimeHelper
                        .TimeStamp2Date(ent.getKey().toString(), "yyyy-MM-dd HH:mm:ss"));
            }
            for (Long key : alarmList) {
                _alarmLogMap.remove(key);
            }
        }
        if (_exceptionLogMap.size() > 0) {
            ArrayList<Long> exceptionList = new ArrayList<>();
            for (Map.Entry<Long, Map<String, ErrorStatisticStruct>> ent : _exceptionLogMap.entrySet()) {
                if (ent.getKey() >= DateTimeHelper.getCurrentTime() - fieryConfig.getKeepdataday() * 86400) {
                    continue;
                }
                exceptionList.add(ent.getKey());
                log.info("del out time exception log,log create_time:" + DateTimeHelper
                        .TimeStamp2Date(ent.getKey().toString(), "yyyy-MM-dd HH:mm:ss"));
            }
            for (Long key : exceptionList) {
                _exceptionLogMap.remove(key);
            }
        }
        //return true;
    }
}
