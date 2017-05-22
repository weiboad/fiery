package org.weiboad.ragnar.server.controller.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.weiboad.ragnar.server.config.FieryConfig;
import org.weiboad.ragnar.server.data.MetaLog;
import org.weiboad.ragnar.server.data.ResponseJson;
import org.weiboad.ragnar.server.search.IndexService;
import org.weiboad.ragnar.server.storage.DBManage;
import org.weiboad.ragnar.server.storage.DBSharder;
import org.weiboad.ragnar.server.util.RPCIDKeySortComparator;
import org.weiboad.ragnar.server.util.TraceIdDecoder;

import java.text.DecimalFormat;
import java.util.*;

@Controller
public class ShowTrace {

    Logger log = LoggerFactory.getLogger(ShowTrace.class);

    @Autowired
    DBManage dbmanager;

    @Autowired
    IndexService indexHelper;

    @Autowired
    FieryConfig fieryConfig;

    private Map<String, Map<String, String>> _tracelist = new TreeMap<String, Map<String, String>>(new RPCIDKeySortComparator());

    private int _renderIndex = 0;

    @RequestMapping(value = "/showtrace", method = RequestMethod.GET)
    public String ShowTracePage(Model model,
                                @RequestParam(value = "traceid", required = false) String traceid,
                                @RequestParam(value = "rpcid", required = false, defaultValue = "0") String selectedrpcid,
                                @RequestParam(value = "oldstyle", required = false, defaultValue = "0") String oldstylebool) {
        DecimalFormat df = new DecimalFormat("######0.0000");

        boolean oldstyle = false;
        if (oldstylebool.trim().equals("1")) {
            oldstyle = true;
            model.addAttribute("oldstyle", 1);
        } else {
            model.addAttribute("oldstyle", 0);
        }

        //clean up the tracelist
        _tracelist.clear();

        //if render the trace
        if (traceid != null && traceid.length() > 6) {
            traceid = traceid.trim();

            //set page parameter
            model.addAttribute("selectedrpcid", selectedrpcid);
            model.addAttribute("traceid", traceid);

            //set the traceid info
            Map<String, String> traceIdInfo = TraceIdDecoder.decodev10(traceid);
            model.addAttribute("idc", traceIdInfo.get("idc"));
            model.addAttribute("ip", traceIdInfo.get("ip"));
            model.addAttribute("timestamp", traceIdInfo.get("timestamp"));
            model.addAttribute("starttime", traceIdInfo.get("time"));
            Date startdate = new Date(Long.parseLong(traceIdInfo.get("time")) * 1000L);
            model.addAttribute("starttimedate", startdate);

            //rpcid and logs data map
            Map<String, String> loglist = new TreeMap<String, String>(new RPCIDKeySortComparator());

            //rpcid and ragnarlog data map
            Map<String, MetaLog> indexlist = new TreeMap<String, MetaLog>(new RPCIDKeySortComparator());

            //get the log from kvDB
            try {
                Long timestampLong = Long.parseLong(traceIdInfo.get("time"));
                DBSharder dbhelper = dbmanager.getDB(timestampLong);
                //prevent expire db
                if (dbhelper == null) {
                    model.addAttribute("tips", "日志信息库未找到！" + timestampLong);
                    return "showtrace_empty";
                }

                String rpcidlist = dbhelper.get(traceid + "_index");

                if (rpcidlist != null) {
                    String[] rpcidArray = rpcidlist.split(",");

                    // remove the repeat
                    for (int rpcidIndex = 0; rpcidIndex < rpcidArray.length; rpcidIndex++) {
                        //log.info("add ragnarlog:" + rpcidIndex + " rpcid:" + rpcidArray[rpcidIndex]);
                        loglist.put(rpcidArray[rpcidIndex], "");
                    }

                    //get the logs by rpcid list
                    for (Map.Entry<String, String> entry : loglist.entrySet()) {
                        String logString = dbhelper.get(traceid + "_" + entry.getKey());
                        loglist.put(entry.getKey(), logString);
                        //log.info(entry.getKey() + " val:" + entry.getValue());
                    }
                }
                //log.debug("traceid:" + traceid + " rpcidlist:" + rpcidlist);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }

            //查询traceid对应的metalog
            try {
                Sort sort;
                sort = new Sort(new SortField("time", SortField.Type.DOUBLE, true));

                Query query;
                query = new TermQuery(new Term("traceid", traceid));

                ResponseJson searchResult = indexHelper.searchByQuery(Long.parseLong(traceIdInfo.get("time")), query, 0, 200, sort);

                List<MetaLog> searchResultMetaList = searchResult.getResult();
                for (MetaLog metalogitem : searchResultMetaList) {
                    //get the parameter to select
                    if (selectedrpcid.equals(metalogitem.getRpcid())) {
                        model.addAttribute("parameter", metalogitem.getParam());
                    }
                    indexlist.put(metalogitem.getRpcid(), metalogitem);
                }

                //log.info("indexlist Count:" + searchResultMetaList.size());

            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
            renderTrace("0", traceid, selectedrpcid, loglist, indexlist, oldstyle);

            model.addAttribute("tracelist", _tracelist);

            //select api log list prepare
            //相关日志渲染
            //todo:网络间隔和实际时间比例展示
            TreeMap<String, HashMap<String, String>> selectLogList = new TreeMap<String, HashMap<String, String>>();
            if (loglist.containsKey(selectedrpcid)) {
                try {

                    //log.debug(loglist.get(selectedrpcid));
                    JsonParser jsonparserHelper = new JsonParser();
                    JsonArray logListRootObj = (JsonArray) jsonparserHelper.parse(loglist.get(selectedrpcid));
                    JsonArray logListArray = (JsonArray) logListRootObj.get(0).getAsJsonObject().get("val");

                    Double beforLogTime = 0d;

                    for (int logindex = 0; logindex < logListArray.size(); logindex++) {

                        JsonObject logItemObj = (JsonObject) logListArray.get(logindex);

                        HashMap<String, String> logitem = new HashMap<>();

                        //inter time warning
                        if (logindex == 0) {
                            beforLogTime = logItemObj.get("e").getAsDouble();
                        }
                        logitem.put("logindex", logindex + "");
                        logitem.put("logintertimewarning", df.format(logItemObj.get("e").getAsDouble() - beforLogTime) + "");
                        beforLogTime = logItemObj.get("e").getAsDouble();

                        //other basic field
                        logitem.put("r", logItemObj.get("r").getAsString());
                        logitem.put("t", logItemObj.get("t").getAsString());
                        logitem.put("e", logItemObj.get("e").getAsString());
                        logitem.put("g", logItemObj.get("g").getAsString());
                        logitem.put("p", logItemObj.get("p").getAsString());
                        logitem.put("l", logItemObj.get("l").getAsString());

                        //make sure the string and obj will decode
                        if (logItemObj.has("m")) {
                            if (logItemObj.get("m").isJsonArray() || logItemObj.get("m").isJsonNull() || logItemObj.get("m").isJsonObject()) {
                                logitem.put("m", logItemObj.get("m").toString());
                            } else {
                                logitem.put("m", logItemObj.get("m").getAsString());
                            }
                        } else {
                            logitem.put("m", "");
                        }

                        if (logItemObj.has("c")) {
                            logitem.put("c", logItemObj.get("c").getAsString()); //?
                        } else {
                            logitem.put("c", "");
                        }
                        selectLogList.put(logindex + "", logitem);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            model.addAttribute("loglist", selectLogList);

            return "showtrace_render";
        } else {
            //waiting the input the traceid
            model.addAttribute("tips", "请填写请求标识 TraceId！");
            return "showtrace_empty";
        }
    }


    //循环方式渲染，用于应对数据不全情况

    public void renderByLog(String renderrRPCid, String renderTraceid, String selectKey,
                            Map<String, String> loglist, Map<String, MetaLog> indexlist) {
        //渲染顺序计数器，用来做日志排序的
        int renderIndex = 0;

        //根据bizlog进行渲染
        for (Map.Entry<String, String> entry : loglist.entrySet()) {
            String itemrpcid = entry.getKey();
            Map<String, String> traceinfo = new HashMap<String, String>();

            //rpcid
            traceinfo.put("rpcid", itemrpcid);
            traceinfo.put("r", itemrpcid);

            //entrance was no tag
            traceinfo.put("tag", "");

            //type 日志类型
            traceinfo.put("type", "");

            //if the metaloginfo existed
            MetaLog metaloginfo;
            if (indexlist.containsKey(itemrpcid)) {
                metaloginfo = indexlist.get(itemrpcid);
                traceinfo.put("url", metaloginfo.getUrl());
            } else {
                metaloginfo = new MetaLog();
                traceinfo.put("url", "====== Meta Info Not Found ===");
            }

            //selected
            if (itemrpcid.equals(selectKey)) {
                traceinfo.put("selected", "selected");
            } else {
                traceinfo.put("selected", "");
            }

            //indent
            traceinfo.put("indent", StringUtils.repeat("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", itemrpcid.split("\\.").length));

            //type
            traceinfo.put("type", metaloginfo.getHttpcode());

            //cost time
            traceinfo.put("elapsed", metaloginfo.getElapsed_ms() + "");

            //server
            traceinfo.put("ip", metaloginfo.getIp());

            //procject
            traceinfo.put("project", metaloginfo.getProject());

            _tracelist.put(renderIndex + "", traceinfo);
            renderIndex++;

            //todo:xhprof日志
            //render the biz log
            //now check
            if (loglist.containsKey(itemrpcid)) {
                String logString = loglist.get(itemrpcid);

                try {

                    JsonParser jsonparserHelper = new JsonParser();
                    JsonArray logArray = (JsonArray) jsonparserHelper.parse(logString);

                    //walk the val log array
                    for (int i = 0; i < logArray.size(); i++) {

                        JsonObject logRoot = logArray.get(i).getAsJsonObject();
                        String logTraceid = logRoot.get("key").getAsString();
                        String logTimestamp = logRoot.get("timestamp").getAsString();
                        JsonArray logItemArray = logRoot.getAsJsonArray("val");

                        //walk the biz log array
                        for (int logItemIndex = 0; logItemIndex < logItemArray.size(); logItemIndex++) {
                            Map<String, String> traceLogInfo = new HashMap<String, String>();

                            JsonObject logItemObj = logItemArray.get(logItemIndex).getAsJsonObject();
                            String logRpcid = logItemObj.get("r").getAsString();
                            String type = logItemObj.get("t").getAsString();
                            String tag = logItemObj.get("g").getAsString();

                            //only show the 9
                            if (!type.equals("9")) {
                                //continue;
                            }

                            traceLogInfo.put("rpcid", itemrpcid);
                            traceLogInfo.put("r", logRpcid);

                            traceLogInfo.put("indent", StringUtils.repeat("&nbsp;-&gt;&nbsp;&nbsp;&nbsp;&nbsp;-", logRpcid.split("\\.").length));
                            traceLogInfo.put("selected", "");
                            traceLogInfo.put("type", type);

                            if (logItemObj.has("c")) {
                                traceLogInfo.put("elapsed", logItemObj.get("c").getAsString());
                            } else {
                                traceLogInfo.put("elapsed", "");
                            }
                            traceLogInfo.put("ip", "--");
                            traceLogInfo.put("project", "--");
                            traceLogInfo.put("tag", tag);

                            //log.info("tag:" + tag);
                            //render the tag log
                            if (tag.equals("curl")) {
                                //curl log render
                                JsonObject logItemMsgObj = logItemObj.get("m").getAsJsonObject();
                                String[] curlUrlString = logItemMsgObj.get("url").getAsString().split("\\?");
                                if (curlUrlString.length > 0) {
                                    traceLogInfo.put("url", curlUrlString[0]);
                                } else {
                                    traceLogInfo.put("url", logItemMsgObj.getAsString());
                                }
                            } else if (tag.equals("mysql")) {
                                //mysql log render
                                JsonObject logItemMsgObj = logItemObj.get("m").getAsJsonObject();
                                if (logItemMsgObj != null) {
                                    String sqlString = logItemMsgObj.get("sql").getAsString();
                                    traceLogInfo.put("url", sqlString);
                                } else {
                                    traceLogInfo.put("url", logItemMsgObj.getAsString());
                                }
                            } else {
                                //mysql log render
                                if (logItemObj != null) {
                                    traceLogInfo.put("url", logItemObj.toString());
                                }
                            }
                            _tracelist.put(renderIndex + "", traceLogInfo);
                            renderIndex++;
                        }

                        //walk the log content


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error(e.getMessage());
                }

            }
        }
    }

    //主流方式：递归方式渲染
    public void renderTrace(String renderrRPCid, String renderTraceid, String selectKey,
                            Map<String, String> loglist, Map<String, MetaLog> indexlist, boolean oldstyle
    ) {

        //todo: 标题过长隐藏功能

        //metalog current render
        MetaLog metaloginfo;

        //entrance api metalog info
        MetaLog entranceMetaLogInfo;

        //render the metalog trace info
        Map<String, String> traceinfo = new HashMap<String, String>();

        //recorde the befor trace info timestamp
        Double beforEndTimeStamp = 0d;

        //total cost time of full request
        Double totalCostTime = 0d;

        DecimalFormat df = new DecimalFormat("######0.0000");

        ///////////////////////
        //API trace info render
        ///////////////////////

        //_tracelist
        //rpcid
        traceinfo.put("rpcid", renderrRPCid);
        traceinfo.put("r", renderrRPCid);

        //entrance was no tag
        traceinfo.put("tag", "api");

        //type 日志类型
        traceinfo.put("type", "");

        if (indexlist.containsKey(renderrRPCid) && !oldstyle) {
            //if the metaloginfo existed
            metaloginfo = indexlist.get(renderrRPCid);
            traceinfo.put("url", metaloginfo.getUrl());
            beforEndTimeStamp = metaloginfo.getTime_raw();
        } else {
            //metaloginfo = new metaLog();
            //traceinfo.put("url", "====== Meta Info Not Found ===");
            if (renderrRPCid.equals("0")) {
                renderByLog(renderrRPCid, renderTraceid, selectKey, loglist, indexlist);
            }
            return;
        }

        //if found the entrance metalog info
        //in fact this sure..
        if (indexlist.containsKey("0")) {
            entranceMetaLogInfo = indexlist.get("0");
            totalCostTime = new Double(entranceMetaLogInfo.getElapsed_ms());
        } else {
            entranceMetaLogInfo = new MetaLog();
        }

        //selected
        if (renderrRPCid.equals(selectKey)) {
            traceinfo.put("selected", "selected");
        } else {
            traceinfo.put("selected", "");
        }

        //indent
        traceinfo.put("indent", StringUtils.repeat("&nbsp;&nbsp;&nbsp;&nbsp;", renderrRPCid.split("\\.").length));

        //type
        traceinfo.put("type", metaloginfo.getHttpcode());

        //cost time
        traceinfo.put("elapsed", metaloginfo.getElapsed_ms() + "");

        //server
        traceinfo.put("ip", metaloginfo.getIp());

        //procject
        traceinfo.put("project", metaloginfo.getProject());

        _tracelist.put(_renderIndex + "", traceinfo);
        _renderIndex++;

        ///////////////////////
        //now render the log on trace
        ///////////////////////
        String logString = loglist.get(renderrRPCid);

        try {

            JsonParser jsonparserHelper = new JsonParser();
            JsonArray logArray = (JsonArray) jsonparserHelper.parse(logString);

            //walk the val log array shell
            for (int i = 0; i < logArray.size(); i++) {

                JsonObject logRoot = logArray.get(i).getAsJsonObject();
                String logTraceid = logRoot.get("key").getAsString();
                String logTimestamp = logRoot.get("timestamp").getAsString();
                JsonArray logItemArray = logRoot.getAsJsonArray("val");

                //walk the biz log array
                for (int logItemIndex = 0; logItemIndex < logItemArray.size(); logItemIndex++) {
                    Map<String, String> traceLogInfo = new HashMap<String, String>();

                    JsonObject logItemObj = logItemArray.get(logItemIndex).getAsJsonObject();
                    String logRpcid = logItemObj.get("r").getAsString();
                    String type = logItemObj.get("t").getAsString();
                    String tag = logItemObj.get("g").getAsString();

                    //only show the 9
                    if (!type.equals("9")) {
                        continue;
                    }

                    //两个埋点时间间隔
                    Double logInterTime = logItemObj.get("e").getAsDouble() - beforEndTimeStamp;
                    //fixed the inter time wrong
                    if (logItemObj.has("c")) {
                        logInterTime = logInterTime - logItemObj.get("c").getAsDouble();
                    }

                    traceLogInfo.put("logintertimewarning", df.format(logInterTime));
                    traceLogInfo.put("logindex", logItemIndex + "");

                    traceLogInfo.put("rpcid", renderrRPCid);
                    traceLogInfo.put("r", logRpcid);

                    traceLogInfo.put("indent", StringUtils.repeat("&nbsp;&nbsp;&nbsp;&nbsp;", logRpcid.split("\\.").length));
                    traceLogInfo.put("selected", "");
                    traceLogInfo.put("type", type);

                    if (logItemObj.has("c")) {
                        traceLogInfo.put("elapsed", logItemObj.get("c").getAsString());
                    } else {
                        traceLogInfo.put("elapsed", "");
                    }
                    traceLogInfo.put("ip", "--");
                    traceLogInfo.put("project", "--");
                    traceLogInfo.put("tag", tag);

                    //log.info("tag:" + tag);
                    //render the tag log
                    if (tag.equals("curl")) {
                        //curl log render
                        JsonObject logItemMsgObj = logItemObj.get("m").getAsJsonObject();
                        String[] curlUrlStr = logItemMsgObj.get("url").getAsString().split("\\?");
                        if (curlUrlStr.length > 0) {
                            traceLogInfo.put("url", curlUrlStr[0]);
                        } else {
                            traceLogInfo.put("url", logItemMsgObj.getAsString());
                        }

                        //timeout lable show
                        try {
                            JsonObject curlinfoRoot = logItemObj.get("m").getAsJsonObject();
                            JsonObject curlinfoobj = (JsonObject) curlinfoRoot.get("info");
                            JsonObject curlinfoErrorObj = (JsonObject) curlinfoobj.get("error");
                            String errorno = curlinfoErrorObj.get("errorno").getAsString();

                            if (errorno.equals("28")) {
                                traceLogInfo.put("istimeout", "1");
                            }

                        } catch (Exception e) {
                            //ignore the errorno field not set
                            //e.printStackTrace();
                            //log.error(e.getMessage());
                        }
                    } else if (tag.equals("mysql")) {
                        //mysql log render
                        JsonObject logItemMsgObj = logItemObj.get("m").getAsJsonObject();

                        if (logItemMsgObj != null) {
                            String sqlStr = logItemMsgObj.get("sql").getAsString();
                            traceLogInfo.put("url", sqlStr);
                        } else {
                            traceLogInfo.put("url", logItemMsgObj.getAsString());
                        }
                    } else {
                        //other log render direct
                        if (logItemObj != null) {
                            traceLogInfo.put("url", logItemObj.toString());
                        }
                    }
                    ///////////////////////
                    //TimeLine Parameter
                    ///////////////////////
                    try {
                        if (beforEndTimeStamp > 0 && totalCostTime > 0) {
                            //start bar
                            Double startBarPercent = ((beforEndTimeStamp - entranceMetaLogInfo.getTime_raw()) / totalCostTime);
                            Double interBarPercent = ((logItemObj.get("e").getAsDouble() - logItemObj.get("c").getAsDouble()) - beforEndTimeStamp) / totalCostTime;
                            Double costBarPercent = logItemObj.get("c").getAsDouble() / totalCostTime;

                            //log.debug("logcost:" + logItemObj.get("c").getAsDouble() + " total:" + totalCostTime);
                            //log.debug("totaltimestamp:" + df.format(entranceMetaLogInfo.getTime_raw()) + "befortime:" + df.format(beforEndTimeStamp) + " total:" + totalCostTime + " start:" + startBarPercent + " inter:" + interBarPercent + " cost:" + costBarPercent);

                            //timeline bar length
                            traceLogInfo.put("timeline_startbar", (int) (startBarPercent * 470) + "");
                            traceLogInfo.put("timeline_interbar", (int) (interBarPercent * 470) + "");
                            traceLogInfo.put("timeline_costbar", (int) (costBarPercent * 470) + "");

                            //if the bar biger than 50px show the time
                            if ((int) (interBarPercent * 470) > 50) {
                                traceLogInfo.put("timeline_interbar_title", df.format(beforEndTimeStamp - entranceMetaLogInfo.getTime_raw()));
                            } else {
                                traceLogInfo.put("timeline_interbar_title", "");
                            }

                            if ((int) (costBarPercent * 470) > 50) {
                                traceLogInfo.put("timeline_costbar_title", logItemObj.get("c").getAsString());
                            } else {
                                traceLogInfo.put("timeline_costbar_title", "");
                            }
                        } else {
                            traceLogInfo.put("timeline_startbar", "0");
                            traceLogInfo.put("timeline_interbar", "0");
                            traceLogInfo.put("timeline_costbar", "0");
                            traceLogInfo.put("timeline_interbar_title", "");
                            traceLogInfo.put("timeline_costbar_title", "");
                        }
                    } catch (Exception e) {
                        //ignore the exception
                        traceLogInfo.put("timeline_startbar", "-1");
                        traceLogInfo.put("timeline_interbar", "-1");
                        traceLogInfo.put("timeline_costbar", "-1");
                        traceLogInfo.put("timeline_interbar_title", "");
                        traceLogInfo.put("timeline_costbar_title", "");
                    }
                    beforEndTimeStamp = logItemObj.get("e").getAsDouble();

                    _tracelist.put(_renderIndex + "", traceLogInfo);
                    _renderIndex++;

                    renderTrace(logRpcid, renderTraceid, selectKey, loglist, indexlist, oldstyle);
                }
                //walk the log content

            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

    }
}
