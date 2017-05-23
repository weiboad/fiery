package org.weiboad.ragnar.server.controller.web;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
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
import org.weiboad.ragnar.server.data.ResponseJson;
import org.weiboad.ragnar.server.data.statics.APITopURLStaticShardCollect;
import org.weiboad.ragnar.server.search.IndexService;
import org.weiboad.ragnar.server.util.DateTimeHelper;

@Controller
public class APITopDetailPage {

    @Autowired
    IndexService indexHelper;

    @Autowired
    FieryConfig fieryConfig;

    @Autowired
    APITopURLStaticShardCollect apiTopURLStaticShardCollect;

    Logger log = LoggerFactory.getLogger(PutMetalog.class);

    @RequestMapping(value = "/apitopdetail", method = RequestMethod.GET)
    public String currentlog(Model model, @RequestParam(value = "url", required = false, defaultValue = "") String keyword) {

        Query query;
        Term term = new Term("url", keyword.trim());
        query = new TermQuery(term);

        Sort sort = new Sort(new SortField("elapsed_ms", SortField.Type.DOUBLE, true));

        ResponseJson result = indexHelper.searchByQuery(DateTimeHelper.getCurrentTime(), query, 0, 1000, sort);
        model.addAttribute("resultlist", result.getResult());
        model.addAttribute("url", keyword);

        return "apitopdetailpage";
    }
}

