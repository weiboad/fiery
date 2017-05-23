package org.weiboad.ragnar.server.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.weiboad.ragnar.server.config.FieryConfig;
import org.weiboad.ragnar.server.statistics.SQLStatics;
import org.weiboad.ragnar.server.util.DateTimeHelper;

import java.util.List;
import java.util.Map;

@Controller
public class SqlStatisticPage {

    @Autowired
    SQLStatics logSql;

    @Autowired
    FieryConfig fieryConfig;

    @RequestMapping(value = "/sqlstatistic", method = RequestMethod.GET)
    public String SqlperformancePage(Model model,
                                     @RequestParam(value = "daytime", required = false) Integer daytime) {

        //校验参数
        if (daytime == null) {
            daytime = 0;
        }

        //list
        List<String> timelist = DateTimeHelper.getDateTimeListForPage(fieryConfig.getKeepdataday());
        model.addAttribute("datelist", timelist);
        model.addAttribute("datelist_selected", daytime);

        Map<String, Map<String, String>> performList = logSql.getAllList(daytime);

        model.addAttribute("list", performList);

        return "sqlstatistic";
    }

    @RequestMapping(value = "/sqlstatisticdetail", method = RequestMethod.GET)
    public String SqlperformShowPage(Model model,
                                     @RequestParam(value = "daytime", required = false) Integer daytime,
                                     @RequestParam(value = "sql", required = false) String sql) {

        //校验参数
        if (daytime == null) {
            daytime = 0;
        }

        //list
        List<String> timelist = DateTimeHelper.getDateTimeListForPage(fieryConfig.getKeepdataday());
        model.addAttribute("datelist", timelist);
        model.addAttribute("datelist_selected", daytime);

        Map<String, Map<String, String>> performList = logSql.getOneData(daytime, sql);

        model.addAttribute("sql", performList.get("sql").get("sql"));
        performList.remove("sql");
        model.addAttribute("sqlpre", sql);
        model.addAttribute("perfomancelist", performList);
        return "sqlstatisticdetail";
    }
}
