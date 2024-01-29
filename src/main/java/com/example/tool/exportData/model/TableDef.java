package com.example.tool.exportData.model;

import java.io.Serializable;

public class TableDef implements Serializable {

    /**
     * 根据表名查找column表，找出每个字段的含义
     */
    private static final long serialVersionUID = 1L;
    private String xh;// 序号
    private String bs;// 标识
    private String lx;// 字段类型
    private String mc;// 名称
    private String ms;// 描述
    private String ystj;// 约束条件
    private String glb;// 关联表
    private String zj;// 主键1:是0:否

    public String getXh() {
        return xh;
    }

    public void setXh(String xh) {
        this.xh = xh;
    }

    public String getBs() {
        return bs;
    }

    public void setBs(String bs) {
        this.bs = bs;
    }

    public String getLx() {
        return lx;
    }

    public void setLx(String lx) {
        this.lx = lx;
    }

    public String getMc() {
        return mc;
    }

    public void setMc(String mc) {
        this.mc = mc;
    }

    public String getMs() {
        return ms;
    }

    public void setMs(String ms) {
        this.ms = ms;
    }

    public String getYstj() {
        return ystj;
    }

    public void setYstj(String ystj) {
        this.ystj = ystj;
    }

    public String getGlb() {
        return glb;
    }

    public void setGlb(String glb) {
        this.glb = glb;
    }

    public String getZj() {
        return zj;
    }

    public void setZj(String zj) {
        this.zj = zj;
    }
}
