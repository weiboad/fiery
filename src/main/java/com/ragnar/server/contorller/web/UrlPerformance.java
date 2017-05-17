package com.ragnar.server.contorller.web;

import com.ragnar.server.statistics.LogAPIStatics;
import com.ragnar.server.util.DateTimeHepler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class UrlPerformance {
    @Autowired
    LogAPIStatics logApi;

    @RequestMapping(value = "/performance", method = RequestMethod.GET)
    public String PerformancePage(Model model,
                                  @RequestParam(value = "daytime", required = false) Integer daytime) {
        //校验参数
        if (daytime == null) {
            daytime = 0;
        }
        Map<String, Map<String, String>> performList = logApi.getPerformList(daytime);
        long timestamp = DateTimeHepler.getCurrentTime();
        long moringTime = DateTimeHepler.getTimesmorning(timestamp);
        model.addAttribute("perfomancelist", performList);
        model.addAttribute("daytime", daytime);
        model.addAttribute("current_date", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime), "yyyy-MM-dd"));
        model.addAttribute("current_date_1", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60), "yyyy-MM-dd"));
        model.addAttribute("current_date_2", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 2), "yyyy-MM-dd"));
        model.addAttribute("current_date_3", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 3), "yyyy-MM-dd"));
        model.addAttribute("current_date_4", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 4), "yyyy-MM-dd"));
        model.addAttribute("current_date_5", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 5), "yyyy-MM-dd"));
        model.addAttribute("current_date_6", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 6), "yyyy-MM-dd"));
        return "logapi_render";
    }

    @RequestMapping(value = "/performanceshow", method = RequestMethod.GET)
    public String PerformanceShowPage(Model model,
                                      @RequestParam(value = "daytime", required = false) Integer daytime,
                                      @RequestParam(value = "url", required = false) String url) {
        Long StartTime = logApi.getStartTime(daytime);
        Long EndTime = StartTime + 24 * 60 * 60 - 1;
        long timestamp = DateTimeHepler.getCurrentTime();
        long moringTime = DateTimeHepler.getTimesmorning(timestamp);
        model.addAttribute("url", url);
        model.addAttribute("daytime", daytime);
        model.addAttribute("current_date", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime), "yyyy-MM-dd"));
        model.addAttribute("current_date_1", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60), "yyyy-MM-dd"));
        model.addAttribute("current_date_2", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 2), "yyyy-MM-dd"));
        model.addAttribute("current_date_3", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 3), "yyyy-MM-dd"));
        model.addAttribute("current_date_4", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 4), "yyyy-MM-dd"));
        model.addAttribute("current_date_5", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 5), "yyyy-MM-dd"));
        model.addAttribute("current_date_6", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 6), "yyyy-MM-dd"));
        Map<String, Map<String, String>> performList = logApi.getPerformShowList(StartTime, EndTime, url);
        model.addAttribute("perfomancelist", performList);
        return "logapigroup_render";
    }
}
