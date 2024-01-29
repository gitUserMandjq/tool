package com.example.tool.exportData.utils;

import com.example.tool.common.utils.StringUtils;
import com.example.tool.exportData.model.Model;
import com.example.tool.exportData.model.TableDef;
import org.apache.poi.util.StringUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GetMysqlComment {
    public static List<Model> getComment(String dbname, Connection conn) {
        List<Model> list = new ArrayList<Model>();
        StringBuffer sql = new StringBuffer("select t.TABLE_NAME,t.TABLE_COMMENT from information_schema.tables t where t.TABLE_TYPE = 'BASE TABLE' and TABLE_schema = '"+dbname+"' ");
        /*if(!StringUtil.isNullOrEmpty(table)){
            sql.append(" and t.TABLE_NAME like '%"+table+"%'");
        }*/
        try {
            PreparedStatement statement = conn.prepareStatement(sql.toString());
            ResultSet tbSet = statement.executeQuery();
            while (tbSet.next())
            {
                Model en = new Model();
                en.setTableName(tbSet.getString("TABLE_NAME"));
                en.setDesc(tbSet.getString("TABLE_COMMENT"));
                list.add(en);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    //查找每个表格的详细信息并封装到List<TableDef>中返回
    public static List<TableDef> getTableDef(String dbname, String tableName,Connection conn) throws Exception {
        //此处查询语句也需要修改
        // select b.ordinal_position,b.COLUMN_name,b.COLUMN_type,b.COLUMN_comment,b.is_nullable,b.column_key from information_schema.TABLES a LEFT JOIN information_schema.COLUMNS b ON a.table_name = b.TABLE_NAME WHERE a.table_schema = '"+tableName+"' "
        // select t.ordinal_position,t.COLUMN_name,t.COLUMN_type,t.COLUMN_comment,t.is_nullable,t.column_key from COLUMNS t WHERE TABLE_schema='gqks' and TABLE_name= '"+tableName+"' "
        // select a.ordinal_position,a.COLUMN_name,a.COLUMN_type,a.COLumn_comment,a.is_nullable,a.column_key from information_schema.COLUMNS a WHERE TABLE_schema='sy-zhhg' and TABLE_name='"+tableName+"'
        PreparedStatement ps = conn.prepareStatement("select a.ordinal_position,a.COLUMN_name,a.COLUMN_type,a.COLumn_comment,a.is_nullable,a.column_key from information_schema.COLUMNS a WHERE TABLE_schema='"+dbname+"' and TABLE_name= '"+tableName+"' ");
        ResultSet rs = ps.executeQuery();
        ResultSetMetaData rsme = rs.getMetaData();
        List<TableDef> list = new ArrayList<TableDef>();
        /*************************遍历结果集元数据获取字段详情数据*****************************/
        while (rs.next())
        {
            TableDef def = new TableDef();
            def.setXh(rs.getString(rsme.getColumnLabel(1)));
            def.setBs(rs.getString(rsme.getColumnLabel(2)));
            def.setLx(rs.getString(rsme.getColumnLabel(3)));
            //字段描述，部分地段是 主题设置（系统内置主题，不可更改） 这样的，因此需要做一下分割
            String fieldDesc = rs.getString(rsme.getColumnLabel(4));

            //先把中文括号转换为英文括号
            if (fieldDesc.contains("（")&& fieldDesc.contains("）")){
                fieldDesc = fieldDesc.replaceAll("（", "(").replaceAll("）", ")");
            }

            if(!StringUtils.isEmpty(fieldDesc)){
                //对英文的符号进行处理,如果ms为空，再处理一次中文符号
                int s=fieldDesc.indexOf("("),d=fieldDesc.indexOf(")");
                if(s!=-1&&d!=-1){
                    String fieldMc = fieldDesc.substring(0, s);
                    String fieldMs = fieldDesc.substring(s + 1, d);
                    def.setMc(fieldMc);
                    def.setMs(fieldMs);
                }else{
                    def.setMc(fieldDesc);
                }
            }
            def.setYstj(rs.getString(rsme.getColumnLabel(5)).equalsIgnoreCase("NO")?"NOT NULL":"");
            def.setGlb("");
            def.setZj(rs.getString(rsme.getColumnLabel(6)).equalsIgnoreCase("PRI")?"1":"0");
            list.add(def);
        }
        return list;
    }
}

