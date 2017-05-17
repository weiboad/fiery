package com.ragnar.server.contorller.search;

import com.ragnar.server.contorller.ragnarlog.PutMetalog;
import com.ragnar.server.data.responseJson;
import com.ragnar.server.search.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class Group {


    @Autowired
    IndexService indexHelper;

    Logger log = LoggerFactory.getLogger(PutMetalog.class);

    @RequestMapping(value = "/group", method = RequestMethod.POST)
    @ResponseBody
    public responseJson SearchKeyword(@RequestParam(value = "timestamp", required = false) String timetamp,
                                      @RequestParam(value = "startpos", required = false) String startpos,
                                      @RequestParam(value = "limit", required = false) String limit

    ) {

        if (timetamp == null || timetamp.length() == 0) {
            responseJson result = new responseJson();
            result.setCode(400);
            result.setMsg("Plasee set the timestamp paramter!");
            return result;
        }

        long timestamplong;

        try {
            timestamplong = Integer.parseInt(timetamp);
        } catch (Exception e) {
            responseJson result = new responseJson();
            result.setCode(400);
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

        responseJson result = indexHelper.searchByGroup(timestamplong, start, datasize);
        return result;
    }
}
