package org.weiboad.ragnar.server.controller.web;

import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
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
import org.weiboad.ragnar.server.search.IndexService;
import org.weiboad.ragnar.server.statistics.api.APIStatisticStruct;
import org.weiboad.ragnar.server.statistics.api.APIStatisticTimeSet;
import org.weiboad.ragnar.server.struct.ResponseJson;
import org.weiboad.ragnar.server.util.DateTimeHelper;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class APIStatisticDetailPage {

    @Autowired
    IndexService indexHelper;

    @Autowired
    FieryConfig fieryConfig;

    @Autowired
    APIStatisticTimeSet apiStatisticTimeSet;

    Logger log = LoggerFactory.getLogger(PutMetalog.class);

    @RequestMapping(value = "/apistatisticdetail", method = RequestMethod.GET)
    public String currentlog(Model model, @RequestParam(value = "url", required = false, defaultValue = "") String keyword,
                             @RequestParam(value = "topdatarange", required = false, defaultValue = "") String dataRange) {

        //date list
        List<String> timelist = DateTimeHelper.getDateTimeListForPage(fieryConfig.getKeepdataday());
        model.addAttribute("datelist", timelist);
        model.addAttribute("datelist_selected", dataRange);

        //now the date render
        long shardtime = DateTimeHelper.getTimesMorning(DateTimeHelper.getBeforeDay(Integer.parseInt(dataRange)));
        ConcurrentHashMap<String, APIStatisticStruct> urllist = apiStatisticTimeSet.getDaySharder(shardtime, false);

        //data range
        Integer dataRangeInt = 0;

        if (dataRange.trim().length() == 0) {
            dataRange = "0";
        }

        model.addAttribute("topdatarange", dataRange);

        try {
            dataRangeInt = Integer.parseInt(dataRange);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return "apistatisticdetail";
        }

        Long startRange = DateTimeHelper.getTimesMorning(DateTimeHelper.getCurrentTime()) - (dataRangeInt * 86400);

        TermQuery termQuery = new TermQuery(new Term("url", keyword.trim()));
        Query rangeQuery = DoublePoint.newRangeQuery("time", startRange, startRange + 86400);
        //rangeStart
        BooleanQuery query = new BooleanQuery.Builder()
                .add(termQuery, BooleanClause.Occur.MUST)
                .add(rangeQuery, BooleanClause.Occur.MUST)
                .build();
/*
        //term query
        String queryString = "url:\"" + keyword.trim() + "\" AND time_raw:[" + startRange + " TO " + (startRange + 86400) + "]";
        log.info("queryString:" + queryString);

        Query query;
        QueryParser parser = new QueryParser("url,time_raw", new StandardAnalyzer());
        try {
            query = parser.parse(queryString);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return "apistatisticdetail";
        }
*/
/*
        Query query;
        Term term = new Term("url", keyword.trim());
        query = new TermQuery(term);

*/

        Sort sort = new Sort(new SortField("elapsed_ms", SortField.Type.DOUBLE, true));
        ResponseJson result = indexHelper.searchByQuery(startRange, query, 0, 1000, sort);
        model.addAttribute("resultlist", result.getResult());
        model.addAttribute("url", keyword);

        return "apistatisticdetail";
    }
}
