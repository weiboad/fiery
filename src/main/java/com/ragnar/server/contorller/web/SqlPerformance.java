package com.ragnar.server.contorller.web;

import com.ragnar.server.statistics.SQLStatics;
import com.ragnar.server.util.DateTimeHepler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class SqlPerformance {
    @Autowired
    SQLStatics logSql;

    @RequestMapping(value = "/sqlperformance", method = RequestMethod.GET)
    public String SqlperformancePage(Model model,
                                     @RequestParam(value = "daytime", required = false) Integer daytime) {
        //校验参数
        if (daytime == null) {
            daytime = 0;
        }
        Map<String, Map<String, String>> performList = logSql.getAllList(daytime);
        long timestamp = DateTimeHepler.getCurrentTime();
        long moringTime = DateTimeHepler.getTimesmorning(timestamp);
        model.addAttribute("list", performList);
        model.addAttribute("daytime", daytime);
        model.addAttribute("current_date", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime), "yyyy-MM-dd"));
        model.addAttribute("current_date_1", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60), "yyyy-MM-dd"));
        model.addAttribute("current_date_2", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 2), "yyyy-MM-dd"));
        model.addAttribute("current_date_3", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 3), "yyyy-MM-dd"));
        model.addAttribute("current_date_4", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 4), "yyyy-MM-dd"));
        model.addAttribute("current_date_5", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 5), "yyyy-MM-dd"));
        model.addAttribute("current_date_6", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 6), "yyyy-MM-dd"));
        return "sqlstatic_render";
    }

    @RequestMapping(value = "/sqlperformshow", method = RequestMethod.GET)
    public String SqlperformShowPage(Model model,
                                     @RequestParam(value = "daytime", required = false) Integer daytime,
                                     @RequestParam(value = "sql", required = false) String sql) {

        long timestamp = DateTimeHepler.getCurrentTime();
        long moringTime = DateTimeHepler.getTimesmorning(timestamp);

        model.addAttribute("daytime", daytime);
        model.addAttribute("current_date", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime), "yyyy-MM-dd"));
        model.addAttribute("current_date_1", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60), "yyyy-MM-dd"));
        model.addAttribute("current_date_2", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 2), "yyyy-MM-dd"));
        model.addAttribute("current_date_3", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 3), "yyyy-MM-dd"));
        model.addAttribute("current_date_4", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 4), "yyyy-MM-dd"));
        model.addAttribute("current_date_5", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 5), "yyyy-MM-dd"));
        model.addAttribute("current_date_6", DateTimeHepler.TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 6), "yyyy-MM-dd"));
        Map<String, Map<String, String>> performList = logSql.getOneData(daytime, sql);

        model.addAttribute("sql", performList.get("sql").get("sql"));
        performList.remove("sql");
        model.addAttribute("perfomancelist", performList);
        return "sqlgroup_render";
    }
}
