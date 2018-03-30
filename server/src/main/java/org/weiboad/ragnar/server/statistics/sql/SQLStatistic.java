package org.weiboad.ragnar.server.statistics.sql;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.visitor.ParameterizedOutputVisitorUtils;
import com.alibaba.druid.util.JdbcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.weiboad.ragnar.server.config.FieryConfig;
import org.weiboad.ragnar.server.statistics.dependapi.DependAPIStatistic;
import org.weiboad.ragnar.server.storage.DBManage;
import org.weiboad.ragnar.server.util.DateTimeHelper;
import org.weiboad.ragnar.server.util.SimHash;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Scope("singleton")
public class SQLStatistic {

    @Autowired
    DBManage dbmanager;

    @Autowired
    DependAPIStatistic logAPi;

    @Autowired
    FieryConfig fieryConfig;

    private Map<SQLKey, Map<Long, SqlStatisticStruct>> _sqlMap = new ConcurrentHashMap<>();
    private Logger log = LoggerFactory.getLogger(SQLStatistic.class);

    public void addSqlMap(String sqlStr, Long hour, Double costTime) {

        if (sqlStr == null || sqlStr.trim().length() == 0) {
            return;
        }

        //clean up the parameter of sql
        String sqlStrPure = SQLUtils.format(sqlStr, JdbcConstants.MYSQL);
        sqlStrPure = ParameterizedOutputVisitorUtils.parameterize(sqlStrPure, JdbcConstants.MYSQL);
        sqlStrPure = sqlStrPure.replaceAll("\\s+", " ");

        SimHash hash1;
        try {
            hash1 = new SimHash(sqlStrPure, 64);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return;
        }

        boolean issame = false;
        for (Map.Entry<SQLKey, Map<Long, SqlStatisticStruct>> entry : _sqlMap.entrySet()) {

            Integer similityThreadHold;
            if (sqlStrPure.length() < 100) {
                similityThreadHold = 8;
            } else if (sqlStrPure.length() < 500) {
                similityThreadHold = 5;
            } else {
                similityThreadHold = 3;
            }

            if (entry.getKey().getHash().hammingDistance(hash1) <= similityThreadHold) {
                addHourMap(entry.getValue(), entry.getKey().getSql(), hour, costTime);
                issame = true;
                break;
            }
        }
        if (!issame) {
            Map<Long, SqlStatisticStruct> hourMap = new HashMap<Long, SqlStatisticStruct>();
            addHourMap(hourMap, sqlStr, hour, costTime);
            SQLKey sqlKey = new SQLKey();
            sqlKey.setHash(hash1);
            sqlKey.setPureSql(sqlStrPure);
            sqlKey.setSql(sqlStr);
            _sqlMap.put(sqlKey, hourMap);
        }
    }

    private void addHourMap(Map<Long, SqlStatisticStruct> hourMap, String sqlStr, Long hour, Double cost) {
        SqlStatisticStruct struct;
        if (!hourMap.containsKey(hour)) {
            struct = new SqlStatisticStruct();
            struct.fastTime = cost;
            struct.slowTime = cost;
            struct.sqlStr = sqlStr;
            if (cost >= 1.0) {
                struct.sumTime[3] = cost;
                struct.sumCount[3] = 1;
            } else if (cost >= 0.5 && cost < 1.0) {
                struct.sumTime[2] = cost;
                struct.sumCount[2] = 1;
            } else if (cost >= 0.2 && cost < 0.5) {
                struct.sumTime[1] = cost;
                struct.sumCount[1] = 1;
            } else {
                struct.sumTime[0] = cost;
                struct.sumCount[0] = 1;
            }
            hourMap.put(hour, struct);
        } else {
            struct = hourMap.get(hour);
            if (struct == null) {
                return;
            }
            struct.sqlStr = sqlStr;
            if (struct.fastTime > cost) {
                struct.fastTime = cost;
            }
            if (struct.slowTime < cost) {
                struct.slowTime = cost;
            }
            if (cost >= 1.0) {
                struct.sumTime[3] += cost;
                struct.sumCount[3] += 1;
            } else if (cost >= 0.5 && cost < 1.0) {
                struct.sumTime[2] += cost;
                struct.sumCount[2] += 1;
            } else if (cost >= 0.2 && cost < 0.5) {
                struct.sumTime[1] += cost;
                struct.sumCount[1] += 1;
            } else {
                struct.sumTime[0] += cost;
                struct.sumCount[0] += 1;
            }
        }
    }

    public Map<String, Map<String, String>> getAllList(Integer daytime) {
        Long start = DateTimeHelper.getTimesMorning(DateTimeHelper.getBeforeDay(daytime));
        Long end = start + 24 * 60 * 60 - 1;
        Map<String, Map<String, String>> list = new HashMap<String, Map<String, String>>();
        for (Map.Entry<SQLKey, Map<Long, SqlStatisticStruct>> ent : _sqlMap.entrySet()) {
            SqlStatisticStruct sqlStatisticStruct = new SqlStatisticStruct();
            sqlStatisticStruct.fastTime = 0.0;
            sqlStatisticStruct.slowTime = 0.0;
            for (Map.Entry<Long, SqlStatisticStruct> ent1 : ent.getValue().entrySet()) {
                if (ent1.getKey() < start || ent1.getKey() > end) {
                    continue;
                }
                sqlStatisticStruct.sqlStr = ent1.getValue().sqlStr;
                if (sqlStatisticStruct.fastTime == 0 || ent1.getValue().fastTime < sqlStatisticStruct.fastTime) {
                    sqlStatisticStruct.fastTime = ent1.getValue().fastTime;
                }
                if (sqlStatisticStruct.slowTime == 0 || ent1.getValue().slowTime > sqlStatisticStruct.slowTime) {
                    sqlStatisticStruct.slowTime = ent1.getValue().slowTime;
                }
                sqlStatisticStruct.sumTime[0] += ent1.getValue().sumTime[0];
                sqlStatisticStruct.sumTime[1] += ent1.getValue().sumTime[1];
                sqlStatisticStruct.sumTime[2] += ent1.getValue().sumTime[2];
                sqlStatisticStruct.sumTime[3] += ent1.getValue().sumTime[3];
                sqlStatisticStruct.sumCount[0] += ent1.getValue().sumCount[0];
                sqlStatisticStruct.sumCount[1] += ent1.getValue().sumCount[1];
                sqlStatisticStruct.sumCount[2] += ent1.getValue().sumCount[2];
                sqlStatisticStruct.sumCount[3] += ent1.getValue().sumCount[3];
            }
            Map<String, String> performJson = new HashMap<String, String>();
            Integer sumcount = sqlStatisticStruct.sumCount[0] + sqlStatisticStruct.sumCount[1] + sqlStatisticStruct.sumCount[2] + sqlStatisticStruct.sumCount[3];
            if (sumcount == 0) {
                continue;
            }
            performJson.put("sql", sqlStatisticStruct.sqlStr);
            //Double fasttime = sqlStatisticStruct.fastTime*1000;
            //Double slowtime = sqlStatisticStruct.slowTime*1000;
            performJson.put("fasttime", getDoubleTime(sqlStatisticStruct.fastTime));
            performJson.put("slowtime", getDoubleTime(sqlStatisticStruct.slowTime));
            performJson.put("sumcount", sumcount.toString());
            //System.out.print(sqlStatisticStruct.sumCount[0]);


            performJson.put("two", getPercent(sqlStatisticStruct.sumCount[0], sumcount));
            performJson.put("five", getPercent(sqlStatisticStruct.sumCount[1], sumcount));
            performJson.put("ten", getPercent(sqlStatisticStruct.sumCount[2], sumcount));
            performJson.put("twenty", getPercent(sqlStatisticStruct.sumCount[3], sumcount));
            list.put(ent.getKey().getSql(), performJson);
        }
        return list;
    }

    public Map<String, Map<String, String>> getOneData(Integer daytime, String sql) {
        Map<String, Map<String, String>> list = new HashMap<String, Map<String, String>>();
        if (_sqlMap.size() == 0) {
            return null;
        }
        SQLKey sqlkey = null;

        log.info("search sql:"+sql);
        for (Map.Entry<SQLKey, Map<Long, SqlStatisticStruct>> ent : _sqlMap.entrySet()) {
            if(ent.getKey().getSql().equalsIgnoreCase(sql)){
                sqlkey = ent.getKey();
            }
        }
        if (sqlkey == null) {
            return null;
        }
        Long start = DateTimeHelper.getTimesMorning(DateTimeHelper.getBeforeDay(daytime));
        Long end = start + 24 * 60 * 60 - 1;
        String sqlStr = "";
        for (Map.Entry<Long, SqlStatisticStruct> hourmap : _sqlMap.get(sqlkey).entrySet()) {
            if (hourmap.getKey() < start || hourmap.getKey() > end) {
                continue;
            }
            Map<String, String> performJson = new HashMap<String, String>();
            Integer sumcount = hourmap.getValue().sumCount[0] + hourmap.getValue().sumCount[1] + hourmap.getValue().sumCount[2] + hourmap.getValue().sumCount[3];
            if (sumcount == 0) {
                continue;
            }
            sqlStr = hourmap.getValue().sqlStr;
            performJson.put("sql",hourmap.getValue().sqlStr);
            performJson.put("fasttime", getDoubleTime(hourmap.getValue().fastTime));
            performJson.put("slowtime", getDoubleTime(hourmap.getValue().slowTime));
            performJson.put("sumcount", sumcount.toString());
            performJson.put("two", getPercent(hourmap.getValue().sumCount[0], sumcount));
            performJson.put("five", getPercent(hourmap.getValue().sumCount[1], sumcount));
            performJson.put("ten", getPercent(hourmap.getValue().sumCount[2], sumcount));
            performJson.put("twenty", getPercent(hourmap.getValue().sumCount[3], sumcount));
            String datetime = DateTimeHelper.TimeStamp2Date(String.valueOf(hourmap.getKey()), "HH");
            list.put(datetime, performJson);
        }
        Map<String, String> sqlMap = new Hashtable<>();
        sqlMap.put("sql", sqlStr);
        list.put("sql", sqlMap);
        return list;
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

    @Async
    @Scheduled(fixedRate = 30 * 1000)
    public void DelOutTimeSqlLog() {
        if (_sqlMap!=null && _sqlMap.size() > 0) {
            Set<Long> delSqlMap = new HashSet<>();

            for (Map.Entry<SQLKey, Map<Long, SqlStatisticStruct>> ent : _sqlMap.entrySet()) {
                for (Map.Entry<Long, SqlStatisticStruct> sqlStatic : ent.getValue().entrySet()) {
                    if (sqlStatic.getKey() < DateTimeHelper.getCurrentTime() - fieryConfig.getKeepdataday() * 86400) {
                        delSqlMap.add(sqlStatic.getKey());
                    }
                }
            }

            for (Map.Entry<SQLKey, Map<Long, SqlStatisticStruct>> ent : _sqlMap.entrySet()) {
                for (Long key : delSqlMap) {
                    if (ent.getValue().containsKey(key)) {
                        ent.getValue().remove(key);
                    }
                }
            }
        }
        //return true;
    }
}
