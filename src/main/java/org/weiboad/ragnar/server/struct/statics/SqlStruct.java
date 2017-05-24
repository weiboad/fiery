package org.weiboad.ragnar.server.struct.statics;

public class SqlStruct {
    //sql语句
    public String sqlStr;
    //执行sql所消耗最短时间
    public Double fastTime;
    //执行sql所消耗最长时间
    public Double slowTime;
    //执行sql所消耗时间分段统计0:0-200,1:200-500,2:50-1000,3:1000-∞ms
    public Double[] sumTime = {0.0, 0.0, 0.0, 0.0};
    //执行sql所消耗时间分段统计0:0-200,1:200-500,2:50-1000,3:1000-∞次数
    public int[] sumCount = {0, 0, 0, 0};
}
