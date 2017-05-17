package com.ragnar.server.contorller.web;

import com.ragnar.server.config.FieryConfig;
import com.ragnar.server.contorller.ragnarlog.PutMetalog;
import com.ragnar.server.data.statics.APITopURLStaticData;
import com.ragnar.server.data.statics.APITopURLStaticShardCollect;
import com.ragnar.server.data.statics.APITopURLStaticURLCollect;
import com.ragnar.server.util.DateTimeHepler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class APITopPage {

    //@Autowired
    //IndexService indexHelper;

    @Autowired
    FieryConfig fieryConfig;

    @Autowired
    APITopURLStaticShardCollect apiTopURLStaticShardCollect;

    Logger log = LoggerFactory.getLogger(PutMetalog.class);

    @RequestMapping(value = "/apitop", method = RequestMethod.GET)
    public String currentlog(Model model, @RequestParam(value = "topdatarange", required = false, defaultValue = "0") String topdaterange) {

        //render the list of date
        List<String> timelist = new ArrayList<>();

        long timestamp = DateTimeHepler.getCurrentTime();
        long moringTime = DateTimeHepler.getTimesmorning(timestamp);

        for (int interDay = 0; interDay < fieryConfig.getKeepdataday(); interDay++) {
            timelist.add(DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - (24 * 60 * 60) * interDay), "yyyy-MM-dd"));
        }

        model.addAttribute("datelist", timelist);
        model.addAttribute("datelist_selected", topdaterange);

        //now the date render
        int datetimeshard = Integer.parseInt(topdaterange);
        long shardtime = moringTime - (24 * 60 * 60) * datetimeshard;
        APITopURLStaticURLCollect urllist = apiTopURLStaticShardCollect.getSharder(shardtime, false);
        if (urllist != null) {
            model.addAttribute("urllist", urllist.getCollectList());
        } else {
            model.addAttribute("urllist", new ArrayList<APITopURLStaticData>());
        }

        return "apitop";
    }
}
