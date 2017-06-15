package org.weiboad.ragnar.server.controller.web;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.weiboad.ragnar.server.controller.ragnarlog.PutMetalog;
import org.weiboad.ragnar.server.search.IndexService;
import org.weiboad.ragnar.server.struct.ResponseJson;
import org.weiboad.ragnar.server.util.DateTimeHelper;

import java.util.HashMap;
import java.util.Map;

@Controller
public class SearchPage {
    @Autowired
    IndexService indexHelper;

    //logger defined
    Logger log = LoggerFactory.getLogger(PutMetalog.class);

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String searchPage(
            Model model,
            @RequestParam(value = "keyword", required = false) String keyword) {

        String[] fieldList = {"uid", "rpcid", "traceid", "rt_type", "url", "param", "ip", "httpcod", "project"};

        Map<String, Float> boosts = new HashMap<>();
        boosts.put("uid", 1.0f);
        boosts.put("ip", 1.0f);
        boosts.put("rpcid", 1.0f);
        boosts.put("traceid", 1.0f);
        boosts.put("rt_type", 1.0f);
        boosts.put("url", 1.0f);
        boosts.put("urlraw", 1.0f);
        boosts.put("param", 1.0f);
        boosts.put("httpcode", 1.0f);
        boosts.put("project", 1.0f);

        MultiFieldQueryParser mulFieldQueryParser = new MultiFieldQueryParser(fieldList, new StandardAnalyzer(), boosts);
        Query query;
        try {
            query = mulFieldQueryParser.parse(keyword);
        } catch (Exception e) {
            model.addAttribute("msg", "query parser error");
            return "search";
        }

        Sort sort = new Sort(new SortField("time", SortField.Type.DOUBLE, true));

        ResponseJson result = indexHelper.searchByQuery(DateTimeHelper.getCurrentTime(), query, 0, 1000, sort);
        model.addAttribute("resultlist", result.getResult());
        model.addAttribute("keyword", keyword);
        return "search";
    }
}
