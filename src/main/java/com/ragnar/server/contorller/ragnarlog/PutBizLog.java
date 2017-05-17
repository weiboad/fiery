package com.ragnar.server.contorller.ragnarlog;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.ragnar.server.Processor.BizLogProcessor;
import com.ragnar.server.data.responseJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class PutBizLog {

    Logger log = LoggerFactory.getLogger(PutBizLog.class);

    @Autowired
    private BizLogProcessor bizLogProcessor;

    @RequestMapping(value = "/log/bizlog/put", method = RequestMethod.POST)
    @ResponseBody
    public responseJson appendLog(@RequestParam(value = "contents", required = false) String contents) {

        responseJson result = new responseJson();

        if (contents == null || contents.length() == 0) {
            result.setCode(401);
            result.setMsg("Plasee set the contents paramter!");
            return result;
        }

        //split the json to by \n
        String[] contentslist = contents.split("\n");
        if (contentslist.length <= 0) {
            result.setCode(405);
            result.setMsg("contents paramter format Wrong!");
            return result;
        }

        for (int i = 0; i < contentslist.length; i++) {
            String jsonstr = contentslist[i].trim();
            JsonParser valueParse = new JsonParser();
            try {
                JsonArray valueArr = (JsonArray) valueParse.parse(jsonstr);
                bizLogProcessor.insertDataQueue(valueArr);
            } catch (Exception e) {
                //e.printStackTrace();
                log.error("parser json wrong:" + jsonstr);
            }
        }

        return result;
    }
}
