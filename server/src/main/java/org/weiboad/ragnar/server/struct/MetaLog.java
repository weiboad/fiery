package org.weiboad.ragnar.server.struct;

import org.apache.lucene.document.*;
import org.apache.lucene.util.BytesRef;

import java.util.Date;

public class MetaLog {


    public String version = "";
    public String rpcid = "";
    public String traceid = "";
    public Double time = 0d;
    public Double time_raw = 0D;
    public Date time_date = new Date(0);

    public Float elapsed_ms = 0.0F;
    public String perf_on = "";
    public String ip = "";
    public String rt_type = "";
    public String uid = "";
    public String url = "";
    public String param = "";
    public String httpcode = "";
    public String project = "";
    //public Map<String, String> extra = new Map<String, String>();

    public Date getTime_date() {
        return time_date;
    }

    public void setTime_date(Date time_date) {
        this.time_date = time_date;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        if (version != null) {
            this.version = version;
        } else {
            this.version = "";
        }
    }

    public String getRpcid() {
        return rpcid;
    }

    public void setRpcid(String rpcid) {
        this.rpcid = rpcid;
    }

    public String getTraceid() {
        return traceid;
    }

    public void setTraceid(String traceid) {
        this.traceid = traceid;
    }

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
        this.time_raw = time;
        this.time_date = new Date(time.longValue() * 1000l);
    }

    public Float getElapsed_ms() {
        return elapsed_ms;
    }

    public void setElapsed_ms(Float elapsed_ms) {
        this.elapsed_ms = elapsed_ms;
    }

    public String getPerf_on() {
        return perf_on;
    }

    public void setPerf_on(String perf_on) {
        this.perf_on = perf_on;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getRt_type() {
        return rt_type;
    }

    public void setRt_type(String rt_type) {
        this.rt_type = rt_type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getHttpcode() {
        return httpcode;
    }

    public void setHttpcode(String httpcode) {
        this.httpcode = httpcode;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public Double getTime_raw() {
        return time_raw;
    }

    public void setTime_raw(Double time_raw) {
        this.time_raw = time_raw;
        this.time = time_raw;
        this.time_date = new Date(time_raw.longValue() * 1000l);
    }
/*
    public Map<String, String> getExtra() {
        return extra;
    }

    public String getExtraStr() {
        return extra.toString();
    }

    public void setExtra(Map<String, String> extra) {
        this.extra = extra;
    }*/

    public Document gDoc() {
        Document doc = new Document();
        Field version = new StringField("version", getVersion(), Field.Store.YES);
        Field rpcid = new StringField("rpcid", getRpcid(), Field.Store.YES);
        Field traceid = new StringField("traceid", getTraceid(), Field.Store.YES);
        Field time = new DoubleDocValuesField("time", getTime());
        Field timeRaw = new StoredField("time_raw", getTime());

        //Field timestamp = new LongPoint("timestamp", lastModified);
        Field elapsed = new DoubleDocValuesField("elapsed_ms", getElapsed_ms());
        Field elapsedRaw = new StoredField("elapsed_ms_raw", getElapsed_ms());

        Field perf_on = new StringField("perf_on", getPerf_on(), Field.Store.YES);
        Field ip = new StringField("ip", getIp(), Field.Store.YES);
        Field rt_type = new StringField("rt_type", getRt_type(), Field.Store.YES);
        Field uid = new StringField("uid", getUid(), Field.Store.YES);
        Field url = new StringField("url", getUrl(), Field.Store.YES);
        Field urlraw = new SortedDocValuesField("urlraw", new BytesRef(getUrl()));
        Field param = new TextField("param", getParam(), Field.Store.YES);
        Field httpcode = new StringField("httpcode", getHttpcode(), Field.Store.YES);
        Field project = new StringField("project", getProject(), Field.Store.YES);
        //todo:extra没有做处理
        //Field extra = new StringField("param", getExtraStr(), Field.Store.YES);

        doc.add(version);
        doc.add(rpcid);
        doc.add(traceid);
        doc.add(time);
        doc.add(timeRaw);

        //doc.add(timestamp);
        doc.add(elapsed);
        doc.add(elapsedRaw);
        doc.add(perf_on);
        doc.add(ip);
        doc.add(rt_type);
        doc.add(uid);
        doc.add(url);
        doc.add(urlraw);
        doc.add(param);
        doc.add(httpcode);
        doc.add(project);
        //doc.add(extra);

        return doc;
    }

    public void init(Document doc) {
        version = doc.get("version");
        rpcid = doc.get("rpcid");
        traceid = doc.get("traceid");
        try {
            setTime(Double.parseDouble((doc.get("time"))));
        } catch (Exception e) {
            setTime(0d);
        }

        try {
            setTime_raw(Double.parseDouble((doc.get("time_raw"))));
        } catch (Exception e) {
            setTime_raw(0d);
        }
        try {
            setElapsed_ms(Float.parseFloat(doc.get("elapsed_ms_raw")));
        } catch (Exception e) {
            setElapsed_ms(0f);
        }
        perf_on = doc.get("perf_on");
        ip = doc.get("ip");
        rt_type = doc.get("rt_type");
        uid = doc.get("uid");
        url = doc.get("url");
        param = doc.get("param");
        httpcode = doc.get("httpcode");
        project = doc.get("project");

        //IndexableField extrafield doc.getField("extra");
        //extra = doc.get("extra");

    }
}
