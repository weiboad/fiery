package org.weiboad.ragnar.server.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.weiboad.ragnar.server.statistics.SQLStatics;
import org.weiboad.ragnar.server.util.DateTimeHelper;

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
        long timestamp = DateTimeHelper.getCurrentTime();
        long moringTime = DateTimeHelper.getTimesMorning(timestamp);
        model.addAttribute("list", performList);
        model.addAttribute("daytime", daytime);
        model.addAttribute("current_date", DateTimeHelper.TimeStamp2Date(String.valueOf(moringTime), "yyyy-MM-dd"));
        model.addAttribute("current_date_1", DateTimeHelper
                .TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60), "yyyy-MM-dd"));
        model.addAttribute("current_date_2", DateTimeHelper
                .TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 2), "yyyy-MM-dd"));
        model.addAttribute("current_date_3", DateTimeHelper
                .TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 3), "yyyy-MM-dd"));
        model.addAttribute("current_date_4", DateTimeHelper
                .TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 4), "yyyy-MM-dd"));
        model.addAttribute("current_date_5", DateTimeHelper
                .TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 5), "yyyy-MM-dd"));
        model.addAttribute("current_date_6", DateTimeHelper
                .TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 6), "yyyy-MM-dd"));
        return "sqlstatic_render";
    }

    @RequestMapping(value = "/sqlperformshow", method = RequestMethod.GET)
    public String SqlperformShowPage(Model model,
                                     @RequestParam(value = "daytime", required = false) Integer daytime,
                                     @RequestParam(value = "sql", required = false) String sql) {

        long timestamp = DateTimeHelper.getCurrentTime();
        long moringTime = DateTimeHelper.getTimesMorning(timestamp);

        model.addAttribute("daytime", daytime);
        model.addAttribute("current_date", DateTimeHelper.TimeStamp2Date(String.valueOf(moringTime), "yyyy-MM-dd"));
        model.addAttribute("current_date_1", DateTimeHelper
                .TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60), "yyyy-MM-dd"));
        model.addAttribute("current_date_2", DateTimeHelper
                .TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 2), "yyyy-MM-dd"));
        model.addAttribute("current_date_3", DateTimeHelper
                .TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 3), "yyyy-MM-dd"));
        model.addAttribute("current_date_4", DateTimeHelper
                .TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 4), "yyyy-MM-dd"));
        model.addAttribute("current_date_5", DateTimeHelper
                .TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 5), "yyyy-MM-dd"));
        model.addAttribute("current_date_6", DateTimeHelper
                .TimeStamp2Date(String.valueOf(moringTime - 24 * 60 * 60 * 6), "yyyy-MM-dd"));
        Map<String, Map<String, String>> performList = logSql.getOneData(daytime, sql);

        model.addAttribute("sql", performList.get("sql").get("sql"));
        performList.remove("sql");
        model.addAttribute("sqlpre", sql);
        model.addAttribute("perfomancelist", performList);
        return "sqlgroup_render";
    }
}
