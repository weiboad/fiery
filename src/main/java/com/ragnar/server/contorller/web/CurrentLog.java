package com.ragnar.server.contorller.web;

import com.ragnar.server.contorller.ragnarlog.PutMetalog;
import com.ragnar.server.data.responseJson;
import com.ragnar.server.search.IndexService;
import com.ragnar.server.util.DateTimeHepler;
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

@Controller
public class CurrentLog {
    @Autowired
    IndexService indexHelper;

    Logger log = LoggerFactory.getLogger(PutMetalog.class);

    @RequestMapping(value = "/currentlog", method = RequestMethod.GET)
    public String currentlog(Model model) {

        Sort sort = new Sort(new SortField("time", SortField.Type.DOUBLE, true));
        Query query = new MatchAllDocsQuery();
        responseJson result = indexHelper.searchByQuery(DateTimeHepler.getCurrentTime(), query, 0, 500, sort);
        model.addAttribute("resultlist", result.getResult());
        return "currentlog";
    }
}
