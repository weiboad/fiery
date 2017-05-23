package org.weiboad.ragnar.server.controller.web;

import org.apache.lucene.search.MatchAllDocsQuery;
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
import org.weiboad.ragnar.server.controller.ragnarlog.PutMetalog;
import org.weiboad.ragnar.server.data.ResponseJson;
import org.weiboad.ragnar.server.search.IndexService;
import org.weiboad.ragnar.server.util.DateTimeHelper;

@Controller
public class RecentRequestPage {
    @Autowired
    IndexService indexHelper;

    Logger log = LoggerFactory.getLogger(PutMetalog.class);

    @RequestMapping(value = "/recentrequest", method = RequestMethod.GET)
    public String currentlog(Model model) {

        Sort sort = new Sort(new SortField("time", SortField.Type.DOUBLE, true));
        Query query = new MatchAllDocsQuery();
        ResponseJson result = indexHelper.searchByQuery(DateTimeHelper.getCurrentTime(), query, 0, 500, sort);
        model.addAttribute("resultlist", result.getResult());
        return "recentrequest";
    }
}
