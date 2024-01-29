package com.example.tool.exportData.model;

import java.io.Serializable;

public class Comment implements Serializable {

    /**
     * Oracle数据库字段标识和注释实体类
     */
    private static final long serialVersionUID = 1L;
    private String bs;// 标识
    private String comment;// 注释

    public String getBs() {
        return bs;
    }

    public void setBs(String bs) {
        this.bs = bs;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}