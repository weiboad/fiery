package com.ragnar.server.contorller.search;

import com.ragnar.server.contorller.ragnarlog.PutMetalog;
import com.ragnar.server.data.responseJson;
import com.ragnar.server.search.IndexService;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class SearchField {

    @Autowired
    IndexService indexHelper;

    Logger log = LoggerFactory.getLogger(PutMetalog.class);

    @RequestMapping(value = "/fulltext", method = RequestMethod.POST)
    @ResponseBody
    public responseJson SearchKeyword(@RequestParam(value = "timestamp", required = false) String timetamp,
                                      @RequestParam(value = "keyword", required = false) String keyword,
                                      @RequestParam(value = "startpos", required = false) String startpos,
                                      @RequestParam(value = "limit", required = false) String limit,
                                      @RequestParam(value = "sortfield", required = false) String sortfield

    ) {

        if (timetamp == null || timetamp.length() == 0 || keyword == null || keyword.length() == 0) {
            responseJson result = new responseJson();
            result.setCode(400);
            result.setMsg("Plasee set the timestamp|keyword paramter!");
            return result;
        }

        long timestamplong;

        try {
            timestamplong = Integer.parseInt(timetamp);
        } catch (Exception e) {
            responseJson result = new responseJson();
            result.setCode(401);
            result.setMsg("Plasee set the timestamp paramter for the long");
            return result;
        }

        int start;
        int datasize;

        try {
            start = Integer.parseInt(startpos);
        } catch (Exception e) {
            start = 0;
        }

        try {
            datasize = Integer.parseInt(limit);
        } catch (Exception e) {
            datasize = 20;
        }

        //limited the sort field
        if (sortfield == null || sortfield.length() == 0 ||
                (!sortfield.equals("elapsed_ms") && !sortfield.equals("time"))) {
            responseJson result = new responseJson();
            result.setCode(402);
            result.setMsg("sort field only support time or elapsed_ms!");
            return result;
        }

        Sort sort;
        if (sortfield.length() > 0) {
            sort = new Sort(new SortField(sortfield, SortField.Type.DOUBLE, true));
        } else {
            sort = new Sort(new SortField("time", SortField.Type.DOUBLE, true));
        }

        String[] fieldList = {"uid", "rpcid", "traceid", "rt_type", "url", "param", "ip", "httpcod", "project"};
        Map<String, Float> boosts = new HashMap<>();
        boosts.put("uid", 1.0f);
        boosts.put("ip", 1.0f);
        boosts.put("rpcid", 1.0f);
        boosts.put("traceid", 1.0f);
        boosts.put("rt_type", 1.0f);
        boosts.put("url", 1.0f);
        boosts.put("param", 1.0f);
        boosts.put("httpcode", 1.0f);
        boosts.put("project", 1.0f);

        MultiFieldQueryParser mulFieldQueryParser = new MultiFieldQueryParser(fieldList, new StandardAnalyzer(), boosts);
        Query query;
        try {
            query = mulFieldQueryParser.parse(keyword);
        } catch (Exception e) {
            responseJson result = new responseJson();
            result.setCode(403);
            result.setMsg("keyword parser wrong...");
            return result;
        }

        responseJson result = indexHelper.searchByQuery(timestamplong, query, start, datasize, sort);
        return result;
    }
}
