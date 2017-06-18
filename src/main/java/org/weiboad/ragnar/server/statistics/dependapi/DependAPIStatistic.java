package org.weiboad.ragnar.server.statistics.dependapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.weiboad.ragnar.server.config.FieryConfig;
import org.weiboad.ragnar.server.storage.DBManage;
import org.weiboad.ragnar.server.util.DateTimeHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Scope("singleton")
public class DependAPIStatistic {

    @Autowired
    DBManage dbmanager;

    private Logger log = LoggerFactory.getLogger(DependAPIStatistic.class);

    private Map<String, Map<Long, DependAPIStatisticStruct>> _performMap = new ConcurrentHashMap<>();

    @Autowired
    FieryConfig fieryConfig;

    public void addPerformMap(String urlStr, Long hour, Double costTime, String httpCode) {
        if (urlStr == null || httpCode == null) {
            return;
        }
        int pIndex = urlStr.indexOf('?');
        if (pIndex != -1) {
            urlStr = urlStr.substring(0, pIndex);
        }
        Map<Long, DependAPIStatisticStruct> hourMap;
        if (_performMap.containsKey(urlStr)) {
            hourMap = _performMap.get(urlStr);
        } else {
            hourMap = new HashMap<Long, DependAPIStatisticStruct>();
        }
        DependAPIStatisticStruct pNode;
        if (!hourMap.containsKey(hour)) {
            pNode = new DependAPIStatisticStruct();
            pNode.setFastTime(costTime);
            pNode.setSlowTime(costTime);
            if (costTime < 0.2) {
                pNode.sumTime[0] = costTime;
                pNode.sumCount[0] = 1;
            } else if (costTime >= 0.2 && costTime < 0.5) {
                pNode.sumTime[1] = costTime;
                pNode.sumCount[1] = 1;
            } else if (costTime >= 0.5 && costTime < 1.0) {
                pNode.sumTime[2] = costTime;
                pNode.sumCount[2] = 1;
            } else {
                pNode.sumTime[3] = costTime;
                pNode.sumCount[3] = 1;
            }
            pNode.codeMap.put(httpCode, 1);
            hourMap.put(hour, pNode);
            _performMap.put(urlStr, hourMap);
        } else {
            pNode = hourMap.get(hour);
            if (pNode == null) {
                return;
            }
            if (pNode.getFastTime() > costTime) {
                pNode.setFastTime(costTime);
            }
            if (pNode.getSlowTime() < costTime) {
                pNode.setSlowTime(costTime);
            }
            if (costTime < 0.2) {
                pNode.sumTime[0] += costTime;
                pNode.sumCount[0] += 1;
            } else if (costTime >= 0.2 && costTime < 0.5) {
                pNode.sumTime[1] += costTime;
                pNode.sumCount[1] += 1;
            } else if (costTime >= 0.5 && costTime < 1.0) {
                pNode.sumTime[2] += costTime;
                pNode.sumCount[2] += 1;
            } else {
                pNode.sumTime[3] += costTime;
                pNode.sumCount[3] += 1;
            }
            if (pNode.codeMap.containsKey(httpCode)) {
                pNode.codeMap.put(httpCode, pNode.codeMap.get(httpCode) + 1);
            } else {
                pNode.codeMap.put(httpCode, 1);
            }
        }
    }

    public Map<String, Map<String, String>> getPerformList(Integer daytime) {
        Long StartTime = DateTimeHelper.getTimesMorning(DateTimeHelper.getBeforeDay(daytime));
        Long EndTime = StartTime + 24 * 60 * 60 - 1;
        Map<String, Map<String, String>> performList = new HashMap<String, Map<String, String>>();
        for (Map.Entry<String, Map<Long, DependAPIStatisticStruct>> entry : _performMap.entrySet()) {
            DependAPIStatisticStruct dayInfo = new DependAPIStatisticStruct();

            dayInfo.setFastTime(0.0);
            dayInfo.setSlowTime(0.0);
            for (Map.Entry<Long, DependAPIStatisticStruct> entry1 : entry.getValue().entrySet()) {
                if (entry1.getKey() <= StartTime || entry1.getKey() >= EndTime) {
                    continue;
                }
                if (dayInfo.fastTime == 0 || entry1.getValue().getFastTime() < dayInfo.fastTime) {
                    dayInfo.fastTime = entry1.getValue().getFastTime();
                }
                if (dayInfo.slowTime == 0 || entry1.getValue().getSlowTime() > dayInfo.slowTime) {
                    dayInfo.slowTime = entry1.getValue().getSlowTime();
                }
                dayInfo.sumTime[0] += entry1.getValue().sumTime[0];
                dayInfo.sumTime[1] += entry1.getValue().sumTime[1];
                dayInfo.sumTime[2] += entry1.getValue().sumTime[2];
                dayInfo.sumTime[3] += entry1.getValue().sumTime[3];
                dayInfo.sumCount[0] += entry1.getValue().sumCount[0];
                dayInfo.sumCount[1] += entry1.getValue().sumCount[1];
                dayInfo.sumCount[2] += entry1.getValue().sumCount[2];
                dayInfo.sumCount[3] += entry1.getValue().sumCount[3];
                for (Map.Entry<String, Integer> entryCode : entry1.getValue().codeMap.entrySet()) {
                    if (dayInfo.codeMap.containsKey(entryCode.getKey())) {
                        dayInfo.codeMap.put(entryCode.getKey(), dayInfo.codeMap.get(entryCode.getKey()) + entryCode.getValue());
                    } else {
                        dayInfo.codeMap.put(entryCode.getKey(), entryCode.getValue());
                    }
                }
            }
            Map<String, String> performjson = new HashMap<String, String>();
            Integer sumcount = dayInfo.sumCount[0] + dayInfo.sumCount[1] + dayInfo.sumCount[2] + dayInfo.sumCount[3];
            if (sumcount == 0) {
                continue;
            }
            performjson.put("fasttime", getDoubleTime(dayInfo.fastTime));
            performjson.put("slowtime", getDoubleTime(dayInfo.slowTime));
            performjson.put("sumcount", sumcount.toString());
            performjson.put("two", getPercent(dayInfo.sumCount[0], sumcount));
            performjson.put("five", getPercent(dayInfo.sumCount[1], sumcount));
            performjson.put("ten", getPercent(dayInfo.sumCount[2], sumcount));
            performjson.put("twenty", getPercent(dayInfo.sumCount[3], sumcount));
            String stab = this.getCodePercent(dayInfo.codeMap, sumcount);
            performjson.put("code", stab);
            performList.put(entry.getKey(), performjson);
        }
        return performList;
    }

    public Map<String, Map<String, String>> getPerformShowList(Long start, Long end, String url) {
        Map<String, Map<String, String>> list = new HashMap<String, Map<String, String>>();
        if (_performMap.size() == 0) {
            return null;
        }
        if (_performMap.get(url) == null) {
            return null;
        }
        for (Map.Entry<Long, DependAPIStatisticStruct> hourmap : _performMap.get(url).entrySet()) {
            if (hourmap.getKey() < start || hourmap.getKey() > end) {
                continue;
            }
            Map<String, String> jsonContent = new HashMap<String, String>();
            Integer sumcount = hourmap.getValue().sumCount[0] + hourmap.getValue().sumCount[1] + hourmap.getValue().sumCount[2] + hourmap.getValue().sumCount[3];
            jsonContent.put("fasttime", getDoubleTime(hourmap.getValue().fastTime));
            jsonContent.put("slowtime", getDoubleTime(hourmap.getValue().slowTime));
            jsonContent.put("sumcount", sumcount.toString());
            jsonContent.put("two", getPercent(hourmap.getValue().sumCount[0], sumcount));
            jsonContent.put("five", getPercent(hourmap.getValue().sumCount[1], sumcount));
            jsonContent.put("ten", getPercent(hourmap.getValue().sumCount[2], sumcount));
            jsonContent.put("twenty", getPercent(hourmap.getValue().sumCount[3], sumcount));
            String stab = this.getCodePercent(hourmap.getValue().codeMap, sumcount);
            jsonContent.put("code", stab);
            String datetime = DateTimeHelper.TimeStamp2Date(String.valueOf(hourmap.getKey()), "HH");
            list.put(datetime, jsonContent);
        }
        return list;
    }

    private String getCodePercent(Map<String, Integer> codeMap, Integer sumcount) {
        String stab = "";
        for (Map.Entry<String, Integer> entry : codeMap.entrySet()) {
            String percent = getPercent(entry.getValue(), sumcount);
            if (entry.getKey().equals("0")) {
                stab += "&nbsp&nbsp&nbsp&nbsp" + entry.getKey() + "：" + percent + "<br>";
            } else {
                stab += entry.getKey() + "：" + percent + "<br>";
            }
        }
        return stab;
    }

    private String getDoubleTime(Double value) {
        String showValue = "";
        if (value != null) {
            String pattern = "##0.00";
            DecimalFormat decimalFormat = new DecimalFormat(pattern);
            showValue = decimalFormat.format(value * 1000);
        }
        return showValue;
    }

    private String getPercent(int a, int b) {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        return df.format(a * 100.00 / b) + "%";
    }

    @Scheduled(fixedRate = 30 * 1000)
    public boolean DelOutTimeSqlLog() {
        if (_performMap.size() > 0) {
            Map<String, ArrayList<Long>> delMap = new Hashtable<>();
            for (Map.Entry<String, Map<Long, DependAPIStatisticStruct>> ent : _performMap.entrySet()) {
                if (ent.getValue().size() > 0) {
                    ArrayList<Long> delList = new ArrayList<>();
                    for (Map.Entry<Long, DependAPIStatisticStruct> hourent : ent.getValue().entrySet()) {
                        if (hourent.getKey() >= DateTimeHelper.getCurrentTime() - fieryConfig.getKeepdataday() * 86400) {
                            continue;
                        }
                        delList.add(hourent.getKey());
                        //_performMap.get(ent.getKey()).remove(hourent.getKey());
                    }
                    delMap.put(ent.getKey(), delList);
                    /*if (_performMap.get(ent.getKey()).size() == 0) {
                        _performMap.remove(ent.getKey());
                    }*/
                }
            }
            for (Map.Entry<String, ArrayList<Long>> urlent : delMap.entrySet()) {
                for (Long key : urlent.getValue()) {
                    _performMap.get(urlent.getKey()).remove(key);
                    log.info("del out time url:" + urlent.getKey() + ",log create_time:" + DateTimeHelper
                            .TimeStamp2Date(key.toString(), "yyyy-MM-dd HH:mm:ss"));
                }
                if (_performMap.get(urlent.getKey()).size() == 0) {
                    _performMap.remove(urlent.getKey());
                }
            }
        }
        return true;
    }
}
