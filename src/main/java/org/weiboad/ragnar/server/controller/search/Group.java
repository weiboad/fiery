package org.weiboad.ragnar.server.controller.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.weiboad.ragnar.server.controller.ragnarlog.PutMetalog;
import org.weiboad.ragnar.server.data.ResponseJson;
import org.weiboad.ragnar.server.search.IndexService;

@RestController
public class Group {


    @Autowired
    IndexService indexHelper;

    Logger log = LoggerFactory.getLogger(PutMetalog.class);

    @RequestMapping(value = "/group", method = RequestMethod.POST)
    @ResponseBody
    public ResponseJson SearchKeyword(@RequestParam(value = "timestamp", required = false) String timetamp,
                                      @RequestParam(value = "startpos", required = false) String startpos,
                                      @RequestParam(value = "limit", required = false) String limit

    ) {

        if (timetamp == null || timetamp.length() == 0) {
            ResponseJson result = new ResponseJson();
            result.setCode(400);
            result.setMsg("Plasee set the timestamp paramter!");
            return result;
        }

        long timestamplong;

        try {
            timestamplong = Integer.parseInt(timetamp);
        } catch (Exception e) {
            ResponseJson result = new ResponseJson();
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

        ResponseJson result = indexHelper.searchByGroup(timestamplong, start, datasize);
        return result;
    }
}
