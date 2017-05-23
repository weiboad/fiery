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
import org.weiboad.ragnar.server.statistics.LogAPIStatics;
import org.weiboad.ragnar.server.util.DateTimeHelper;

import java.util.List;
import java.util.Map;

@Controller
public class DependStatisticPage {
    @Autowired
    LogAPIStatics logApi;

    @Autowired
    FieryConfig fieryConfig;

    private Logger log = LoggerFactory.getLogger(DependStatisticPage.class);

    @RequestMapping(value = "/dependstatistic", method = RequestMethod.GET)
    public String PerformancePage(Model model,
                                  @RequestParam(value = "daytime", required = false) Integer daytime) {
        //校验参数
        if (daytime == null) {
            daytime = 0;
        }

        //list
        List<String> timelist = DateTimeHelper.getDateTimeListForPage(fieryConfig.getKeepdataday());
        model.addAttribute("datelist", timelist);
        model.addAttribute("datelist_selected", daytime);

        Map<String, Map<String, String>> performList = logApi.getPerformList(daytime);
        model.addAttribute("perfomancelist", performList);

        return "dependstatistic";
    }

    @RequestMapping(value = "/dependstatisticdetail", method = RequestMethod.GET)
    public String PerformanceShowPage(Model model,
                                      @RequestParam(value = "daytime", required = false) Integer daytime,
                                      @RequestParam(value = "url", required = false) String url) {
        //校验参数
        if (daytime == null) {
            daytime = 0;
        }

        model.addAttribute("url", url);

        //list
        List<String> timelist = DateTimeHelper.getDateTimeListForPage(fieryConfig.getKeepdataday());
        model.addAttribute("datelist", timelist);
        model.addAttribute("datelist_selected", daytime);

        Long StartTime = DateTimeHelper.getTimesMorning(DateTimeHelper.getBeforeDay(daytime));
        Long EndTime = StartTime + 24 * 60 * 60 - 1;
        Map<String, Map<String, String>> performList = logApi.getPerformShowList(StartTime, EndTime, url);
        model.addAttribute("perfomancelist", performList);
        return "dependstatisticdetail";
    }
}
