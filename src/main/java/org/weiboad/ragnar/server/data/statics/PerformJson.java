package org.weiboad.ragnar.server.data.statics;

public class PerformJson {
    //public String _url;
    //耗时最短的时间
    public Double fastTime = 0.0;
    //耗时最长的时间
    public Double slowTime = 0.0;
    //统计分段耗时时长
    public int sumcount = 0;
    //统计分段耗时时长的个数
    public int[] sumCount = {0, 0, 0, 0};
    public String[] percent = {"", "", "", ""};
    //统计url返回码的次数
    //public Map<String,Float> codeMap = new HashMap<String,Float>();
    public String codePercent;
}
