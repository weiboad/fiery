package org.weiboad.ragnar.server.controller.ragnarlog;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.weiboad.ragnar.server.data.MetaLog;
import org.weiboad.ragnar.server.data.ResponseJson;
import org.weiboad.ragnar.server.processor.MetaLogProcessor;
import org.weiboad.ragnar.server.search.IndexService;

@RestController
public class PutMetalog {

    @Autowired
    IndexService indexHelper;

    @Autowired
    MetaLogProcessor metaLogProcessor;

    Logger log = LoggerFactory.getLogger(PutMetalog.class);

    @RequestMapping(value = "/log/metalog/put", method = RequestMethod.POST)
    @ResponseBody
    public ResponseJson appendindex(@RequestParam(value = "contents", required = false) String contents) {

        Gson gsonHelper = new Gson();

        ResponseJson result = new ResponseJson();

        if (contents == null || contents.length() == 0) {
            result.setCode(400);
            result.setMsg("Plasee set the contents paramter!");
            return result;
        }

        //url decode the contents
        try {
            contents = java.net.URLDecoder.decode(contents, "utf-8");
        } catch (Exception e) {
            //e.printStackTrace();
            result.setCode(402);
            result.setMsg("contents decode wrong!:" + e.getMessage());
            return result;
        }

        //split the json to by \r\n
        String[] contentslist = contents.split("\n");
        if (contentslist.length > 0) {
            for (int i = 0; i < contentslist.length; i++) {
                String jsonstr = contentslist[i].trim();

                //base64 decode
                try {
                    sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
                    jsonstr = new String(decoder.decodeBuffer(jsonstr));
                    String[] metalogPack = jsonstr.trim().split("\n");

                    //remove the es info
                    if (metalogPack.length == 2) {
                        jsonstr = metalogPack[1];

                        //log.info("jsonStr:"+jsonstr);
                        MetaLog metalog = gsonHelper.fromJson(jsonstr, MetaLog.class);
                        indexHelper.insertProcessQueue(metalog);
                        metaLogProcessor.insertDataQueue(metalog);
                    } else {
                        jsonstr = "";
                    }


                } catch (Exception e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            result.setCode(401);
            result.setMsg("contents paramter format Wrong!");
            return result;
        }

        return result;
    }
}
