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
import org.weiboad.ragnar.server.statistics.api.APIStatisticURLSet;
import org.weiboad.ragnar.server.util.DateTimeHelper;

import java.util.ArrayList;
import java.util.List;

@Controller
public class APIStatisticPage {

    //@Autowired
    //IndexService indexHelper;

    @Autowired
    FieryConfig fieryConfig;

    @Autowired
    APIStatisticTimeSet apiStatisticTimeSet;

    Logger log = LoggerFactory.getLogger(PutMetalog.class);

    @RequestMapping(value = "/apistatistic", method = RequestMethod.GET)
    public String currentlog(Model model, @RequestParam(value = "topdatarange", required = false, defaultValue = "0") String topdaterange) {

        //date list
        List<String> timelist = DateTimeHelper.getDateTimeListForPage(fieryConfig.getKeepdataday());
        model.addAttribute("datelist", timelist);
        model.addAttribute("datelist_selected", topdaterange);

        //now the date render
        long shardtime = DateTimeHelper.getTimesMorning(DateTimeHelper.getBeforeDay(Integer.parseInt(topdaterange)));
        APIStatisticURLSet urllist = apiStatisticTimeSet.getSharder(shardtime, false);
        if (urllist != null) {
            model.addAttribute("urllist", urllist.getCollectList());
        } else {
            model.addAttribute("urllist", new ArrayList<APIStatisticStruct>());
        }

        return "apistatistic";
    }
}
