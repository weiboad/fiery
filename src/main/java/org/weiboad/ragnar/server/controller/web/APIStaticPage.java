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
import org.weiboad.ragnar.server.data.statics.APITopURLStaticData;
import org.weiboad.ragnar.server.data.statics.APITopURLStaticShardCollect;
import org.weiboad.ragnar.server.data.statics.APITopURLStaticURLCollect;
import org.weiboad.ragnar.server.util.DateTimeHelper;

import java.util.ArrayList;
import java.util.List;

@Controller
public class APIStaticPage {

    //@Autowired
    //IndexService indexHelper;

    @Autowired
    FieryConfig fieryConfig;

    @Autowired
    APITopURLStaticShardCollect apiTopURLStaticShardCollect;

    Logger log = LoggerFactory.getLogger(PutMetalog.class);

    @RequestMapping(value = "/apistatistic", method = RequestMethod.GET)
    public String currentlog(Model model, @RequestParam(value = "topdatarange", required = false, defaultValue = "0") String topdaterange) {

        //date list
        List<String> timelist = DateTimeHelper.getDateTimeListForPage(fieryConfig.getKeepdataday());
        model.addAttribute("datelist", timelist);
        model.addAttribute("datelist_selected", topdaterange);

        //now the date render
        long shardtime = DateTimeHelper.getTimesMorning(DateTimeHelper.getBeforeDay(Integer.parseInt(topdaterange)));
        APITopURLStaticURLCollect urllist = apiTopURLStaticShardCollect.getSharder(shardtime, false);
        if (urllist != null) {
            model.addAttribute("urllist", urllist.getCollectList());
        } else {
            model.addAttribute("urllist", new ArrayList<APITopURLStaticData>());
        }

        return "apistatistic";
    }
}
