package com.example.tool.exportData.model;

import java.io.Serializable;
import java.util.List;

public class Model implements Serializable {
    /**
     * 表格实体类
     */
    private static final long serialVersionUID = 1L;
    private String tableName;// 表格名称
    private String desc;// 表格功能说明
    private String yt;// 表格用途;
    private List<TableDef> defList;// 表格字段定义

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getYt() {
        return yt;
    }

    public void setYt(String yt) {
        this.yt = yt;
    }

    public List<TableDef> getDefList() {
        return defList;
    }

    public void setDefList(List<TableDef> defList) {
        this.defList = defList;
    }
}