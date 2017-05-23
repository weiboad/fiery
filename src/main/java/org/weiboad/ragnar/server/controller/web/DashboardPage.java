package org.weiboad.ragnar.server.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.weiboad.ragnar.server.struct.statics.APIStaticTimeSet;
import org.weiboad.ragnar.server.processor.BizLogProcessor;
import org.weiboad.ragnar.server.processor.MetaLogProcessor;
import org.weiboad.ragnar.server.search.IndexService;
import org.weiboad.ragnar.server.statistics.ErrorStatics;
import org.weiboad.ragnar.server.statistics.DependAPIStatics;
import org.weiboad.ragnar.server.statistics.SQLStatics;
import org.weiboad.ragnar.server.storage.DBManage;

import java.util.Map;

@Controller
public class DashboardPage {

    @Autowired
    IndexService indexService;

    @Autowired
    DBManage dbManage;

    @Autowired
    ErrorStatics errorStatics;

    @Autowired
    DependAPIStatics dependAPIStatics;

    @Autowired
    SQLStatics sqlStatics;

    @Autowired
    APIStaticTimeSet apiStaticTimeSet;

    @Autowired
    BizLogProcessor bizLogProcessor;

    @Autowired
    MetaLogProcessor metaLogProcessor;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String IndexPage(Model model) {

        //write index list
        Map<String, Map<String, String>> writeIndexInfo = indexService.getWriteIndexInfo();
        model.addAttribute("writeInfoList", writeIndexInfo);

        //search index list
        Map<String, Map<String, String>> searchIndexInfo = indexService.getSearchIndexInfo();
        model.addAttribute("searchInfoList", searchIndexInfo);

        //multisearch index doc count
        int indexedDocCount = indexService.getIndexedDocCount();
        model.addAttribute("indexedDocCount", indexedDocCount);

        //db list
        Map<String, String> dbInfoList = dbManage.getDbList();
        model.addAttribute("dbInfoList", dbInfoList);

        //error statics
        Map<String, Integer> errorStatic = errorStatics.getErrorStatics();
        model.addAttribute("errorStatic", errorStatic);

        //alarm statics
        Map<String, Integer> alarmStatic = errorStatics.getAlaramStatics();
        model.addAttribute("alarmStatic", alarmStatic);

        //exception statics
        Map<String, Integer> exceptionStatic = errorStatics.getExceptionStatics();
        model.addAttribute("exceptionStatic", exceptionStatic);

        //api top statics
        Map<String, Integer> apitopStatic = apiStaticTimeSet.getAPITOPStatics();
        model.addAttribute("apitopStatic", apitopStatic);

        model.addAttribute("metalogQueueLen", metaLogProcessor.getQueueLen());

        model.addAttribute("bizlogQueueLen", bizLogProcessor.getQueueLen());

        return "dashboard";
    }
}
