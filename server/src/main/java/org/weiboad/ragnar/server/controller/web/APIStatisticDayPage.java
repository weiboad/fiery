package org.weiboad.ragnar.server.controller.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.weiboad.ragnar.server.config.FieryConfig;
import org.weiboad.ragnar.server.controller.ragnarlog.PutMetalog;
import org.weiboad.ragnar.server.statistics.api.APIStatisticStruct;
import org.weiboad.ragnar.server.statistics.api.APIStatisticTimeSet;
import org.weiboad.ragnar.server.util.DateTimeHelper;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class APIStatisticDayPage {

    @Autowired
    private FieryConfig fieryConfig;

    @Autowired
    private APIStatisticTimeSet apiStatisticTimeSet;

    Logger log = LoggerFactory.getLogger(PutMetalog.class);

    @RequestMapping(value = "/apistatisticday", method = RequestMethod.GET)
    public String currentlog(Model model,
                             @RequestParam(value = "topdatarange", required = false, defaultValue = "0") String topdaterange,
                             @RequestParam(value = "url", required = false, defaultValue = "") String url
    ) {

        //date list
        List<String> timelist = DateTimeHelper.getDateTimeListForPage(fieryConfig.getKeepdataday());
        model.addAttribute("datelist", timelist);
        model.addAttribute("datelist_selected", topdaterange);

        model.addAttribute("url", url);

        //now the date render
        long shardtime = DateTimeHelper.getTimesMorning(DateTimeHelper.getBeforeDay(Integer.parseInt(topdaterange)));

        TreeMap<Long, APIStatisticStruct> urlList = apiStatisticTimeSet.getHourDetail(url, shardtime);

        //log.info("size:" + urlList.size());

        model.addAttribute("urllist", urlList);

        //http code
        TreeMap<String, Long> httpCodeMap = new TreeMap<>();
        for (Map.Entry<Long, APIStatisticStruct> timeItem : urlList.entrySet()) {
            for (ConcurrentHashMap.Entry<String, AtomicLong> httpitem : timeItem.getValue().getCode_count().entrySet()) {
                if (!httpCodeMap.containsKey(httpitem.getKey())) {
                    httpCodeMap.put(httpitem.getKey(), httpitem.getValue().longValue());
                } else {
                    httpCodeMap.put(httpitem.getKey(), httpCodeMap.get(httpitem.getKey()) + httpitem.getValue().longValue());
                }
            }

        }

        model.addAttribute("httpcode", httpCodeMap);

        return "apistatisticday";
    }
}
