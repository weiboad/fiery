package org.weiboad.ragnar.server.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.weiboad.ragnar.server.statistics.ErrorStatics;
import org.weiboad.ragnar.server.statistics.LogAPIStatics;
import org.weiboad.ragnar.server.util.DateTimeHelper;

import java.util.Map;

@Controller
public class ExceptionStatics {
    @Autowired
    ErrorStatics errorlog;
    @Autowired
    LogAPIStatics logApi;

    @RequestMapping(value = "/errorstatic", method = RequestMethod.GET)
    public String PerformancePage(Model model,
                                  @RequestParam(value = "daytime", required = false) Integer daytime) {
        //校验参数
        if (daytime == null) {
            daytime = 0;
        }
        Long IndexTime = logApi.getStartTime(daytime);
        Map<String, Map<String, String>> errorList = errorlog.getErrorData(5, IndexTime);
        Map<String, Map<String, String>> alarmList = errorlog.getErrorData(6, IndexTime);
        Map<String, Map<String, String>> exceptionList = errorlog.getErrorData(7, IndexTime);
        model.addAttribute("error", errorList);
        model.addAttribute("error_count", errorList.size());
        model.addAttribute("alarm", alarmList);
        model.addAttribute("exception", exceptionList);
        model.addAttribute("alarm_count", alarmList.size());
        model.addAttribute("exception_count", exceptionList.size());
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
        return "errorlog_render";
    }

    @RequestMapping(value = "/errorstatic/del", method = RequestMethod.GET)
    public String PerformancePage(Model model,
                                  @RequestParam(value = "daytime", required = false) String daytime,
                                  @RequestParam(value = "hashcode", required = false) String hashcode,
                                  @RequestParam(value = "type", required = false) String type) {
        Long day = logApi.getStartTime(Integer.valueOf(daytime));
        String retMsg = errorlog.DelLogInfo(hashcode, day, Integer.valueOf(type));
        model.addAttribute("msg", retMsg);
        model.addAttribute("daytime", daytime);
        return "delerrorlog";
    }
}
