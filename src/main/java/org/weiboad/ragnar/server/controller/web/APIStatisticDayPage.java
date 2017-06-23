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
import java.util.concurrent.ConcurrentHashMap;

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
        ConcurrentHashMap<Long, APIStatisticStruct> urlList = apiStatisticTimeSet.getHourDetail(url, shardtime);

        //log.info("size:" + urlList.size());

        model.addAttribute("urllist", urlList);

        return "apistatisticday";
    }
}
