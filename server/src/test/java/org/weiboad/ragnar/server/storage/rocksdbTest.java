package org.weiboad.ragnar.server.storage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.weiboad.ragnar.server.util.DateTimeHelper;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class rocksdbTest {

    @Autowired
    DBManage dbManage;

    @Test
    public void testRocksdbRBAC() {
        File dbDir = new File("./db");
        if (!dbDir.exists()) {
            dbDir.mkdir();
        }
        DBSharder dbhelper = dbManage.getDB(DateTimeHelper.getCurrentTime());
        dbhelper.del("test");
        assertThat(dbhelper.put("test", "yes")).isTrue();
        assertThat(dbhelper.merge("test", "yes")).isTrue();
        assertThat(dbhelper.get("test")).isEqualTo("yes,yes");
        assertThat(dbhelper.del("test")).isTrue();
    }

}
