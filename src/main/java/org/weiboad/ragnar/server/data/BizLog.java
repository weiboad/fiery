package org.weiboad.ragnar.server.data;

public class BizLog {

    public String t;//log type: 1-debug 2-trace 3-notice 4-info 5-error 6-emergency 7-exception 8-xhprof 9-performance
    public Double e;//timestamp 1485102015.0934

    public String g;//group name
    public String p;//file path
    public String l;//file line

    public String r;//rpcid
    public Double c;//cost time

    /*public String m;

    public String getM() {
        return m;
    }
    public void setM(String m) {
        this.m = m;
    }*/

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public Double getE() {
        return e;
    }

    public void setE(Double e) {
        this.e = e;
    }

    public String getG() {
        return g;
    }

    public void setG(String g) {
        this.g = g;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public String getL() {
        return l;
    }

    public void setL(String l) {
        this.l = l;
    }

    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }

    public Double getC() {
        return c;
    }

    public void setC(Double c) {
        this.c = c;
    }

}
