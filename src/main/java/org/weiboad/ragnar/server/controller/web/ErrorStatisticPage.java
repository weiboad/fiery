package org.weiboad.ragnar.server.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.weiboad.ragnar.server.config.FieryConfig;
import org.weiboad.ragnar.server.statistics.ErrorStatics;
import org.weiboad.ragnar.server.statistics.LogAPIStatics;
import org.weiboad.ragnar.server.util.DateTimeHelper;

import java.util.List;
import java.util.Map;

@Controller
public class ErrorStatisticPage {

    @Autowired
    ErrorStatics errorlog;

    @Autowired
    LogAPIStatics logApi;

    @Autowired
    FieryConfig fieryConfig;

    @RequestMapping(value = "/errorstatic", method = RequestMethod.GET)
    public String PerformancePage(Model model,
                                  @RequestParam(value = "daytime", required = false) Integer daytime) {
        //校验参数
        if (daytime == null) {
            daytime = 0;
        }

        Long IndexTime = DateTimeHelper.getTimesMorning(DateTimeHelper.getBeforeDay(daytime));

        Map<String, Map<String, String>> errorList = errorlog.getErrorData(5, IndexTime);
        Map<String, Map<String, String>> alarmList = errorlog.getErrorData(6, IndexTime);
        Map<String, Map<String, String>> exceptionList = errorlog.getErrorData(7, IndexTime);

        model.addAttribute("error", errorList);
        model.addAttribute("error_count", errorList.size());
        model.addAttribute("alarm", alarmList);
        model.addAttribute("exception", exceptionList);
        model.addAttribute("alarm_count", alarmList.size());
        model.addAttribute("exception_count", exceptionList.size());

        //list
        List<String> timelist = DateTimeHelper.getDateTimeListForPage(fieryConfig.getKeepdataday());
        model.addAttribute("datelist", timelist);
        model.addAttribute("datelist_selected", daytime);

        return "errorstatic";
    }

    @RequestMapping(value = "/errorstatic/del", method = RequestMethod.GET)
    public String PerformancePage(Model model,
                                  @RequestParam(value = "daytime", required = false) String daytime,
                                  @RequestParam(value = "hashcode", required = false) String hashcode,
                                  @RequestParam(value = "type", required = false) String type) {
        Long day = DateTimeHelper.getTimesMorning(DateTimeHelper.getTimesMorning(DateTimeHelper.getBeforeDay(Integer.valueOf(daytime))));
        String retMsg = errorlog.DelLogInfo(hashcode, day, Integer.valueOf(type));
        model.addAttribute("msg", retMsg);
        model.addAttribute("daytime", daytime);
        return "errorstaticremove";
    }
}
