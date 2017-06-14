package org.weiboad.ragnar.server.search;

import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.weiboad.ragnar.server.struct.MetaLog;
import org.weiboad.ragnar.server.struct.ResponseJson;
import org.weiboad.ragnar.server.util.DateTimeHelper;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class luceneTest {

    @Autowired
    IndexService indexService;

    @Test
    public void testLucene() {
        MetaLog metaLog = new MetaLog();
        metaLog.setElapsed_ms(1.0F);
        metaLog.setTime(Double.valueOf( DateTimeHelper.getCurrentTime()));
        metaLog.setHttpcode("200");
        metaLog.setIp("10.10.10.10");
        metaLog.setPerf_on("0");
        metaLog.setProject("test");
        metaLog.setUrl("fieryTest");
        metaLog.setRpcid("0.1");
        metaLog.setTraceid("0000000");
        metaLog.setVersion("0.0");
        metaLog.setUid("100000");

        //insert the queue
        String timeShard = String.valueOf(DateTimeHelper.getTimesMorning(DateTimeHelper.getCurrentTime()));
        indexService.addIndex(timeShard,metaLog);
        indexService.addIndex(timeShard,metaLog);
        indexService.addIndex(timeShard,metaLog);
        indexService.addIndex(timeShard,metaLog);

        indexService.commitChange(timeShard);

        //the write index more than zero
        assertThat(indexService.getWriteIndexInfo().size()).isGreaterThan(0);
        try {
            TimeUnit.SECONDS.sleep(5);
        }catch (Exception e){

        }

        //search test
        Query query;
        query = new MatchAllDocsQuery();

        Sort sort = new Sort(new SortField("elapsed_ms", SortField.Type.DOUBLE, true));

        ResponseJson result = indexService.searchByQuery(DateTimeHelper.getCurrentTime(), query, 0, 1000, sort);

        //search index init success
        assertThat(indexService.getSearchIndexInfo().size()).isGreaterThan(0);
        assertThat(result.getResult().size()).isGreaterThan(0);

    }
}
