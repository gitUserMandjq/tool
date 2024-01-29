package com.example.tool.exportData.utils;

import com.example.tool.common.utils.StringUtils;
import com.example.tool.exportData.model.Comment;
import com.example.tool.exportData.model.Model;
import com.example.tool.exportData.model.TableDef;
import org.apache.poi.util.StringUtil;

import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class GetOracleComment {
    public static List<Model> getComment(String table, Connection conn) {
        //连接Oracle数据库获取数据库的表名和表注释
        table = table.toUpperCase();
        List<Model> list = new ArrayList<Model>();
        StringBuffer sql = new StringBuffer("select  t.TABLE_NAME, t.COMMENTS  from user_tab_comments t  where t.TABLE_TYPE = 'TABLE' ");
        if(!StringUtils.isEmpty(table)){
            sql.append(" and t.TABLE_NAME like '%"+table+"%'");
        }
        try {
            PreparedStatement statement = conn.prepareStatement(sql.toString());
            ResultSet tbSet = statement.executeQuery();
            while (tbSet.next())
            {
                Model en = new Model();
                en.setTableName(tbSet.getString("TABLE_NAME"));
                en.setDesc(tbSet.getString("COMMENTS"));
                list.add(en);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<TableDef> getTableDef(String tableName, Connection conn) throws Exception {
        //从user_cons_columns中查字段id，字段名称，字段数据类型，字段是否非空
        PreparedStatement ps1 = conn.prepareStatement("select column_id,column_name,data_type,nullable,data_length from user_tab_columns where table_name = '"+tableName+"'");
        //从user_cons_columns中查约束类型为P的字段名称（即主键）
        PreparedStatement ps2 = conn.prepareStatement("select COLUMN_name from user_cons_columns where table_name = '"+tableName+"' and constraint_name = (select constraint_name from user_constraints where Table_Name='"+tableName+"' and constraint_type ='P')");
        ResultSet resultSet1 = ps1.executeQuery();
        ResultSet resultSet2 = ps2.executeQuery();
        String primaryKeyColumnName="";
        while (resultSet2.next()){
            primaryKeyColumnName = resultSet2.getString("COLUMN_NAME");
        }
        ResultSetMetaData rsme1 = resultSet1.getMetaData();
        List<TableDef> list = new ArrayList<TableDef>();
        /*******************设置表格详细内容*********************/
        while (resultSet1.next()){
            TableDef def = new TableDef();
            def.setXh(resultSet1.getString(rsme1.getColumnLabel(1)));
            def.setBs(resultSet1.getString(rsme1.getColumnLabel(2)));
            //字段类型此处须作拼接
            String LxPrefix = resultSet1.getString(rsme1.getColumnLabel(3));
            String LxPostfix = resultSet1.getString(rsme1.getColumnLabel(5));
            //如果是NVARCHAR类型，需要对长度进行除以2处理
            if ("NVARCHAR2".equalsIgnoreCase(LxPrefix)){
                int realLength = Integer.valueOf(LxPostfix) / 2;
                LxPostfix = String.valueOf(realLength);
            }
            StringBuffer Lx = new StringBuffer();
            Lx.append(LxPrefix).append("(").append(LxPostfix).append(")");
            def.setLx(Lx.toString());

            def.setYstj(resultSet1.getString(rsme1.getColumnLabel(4)).equalsIgnoreCase("N")?"NOT NULL":"");
            def.setGlb("");
            def.setZj(def.getBs().equalsIgnoreCase(primaryKeyColumnName)?"1":"0");
            list.add(def);
        }
        /*************************设置字段名称和字段描述*************************/
        List<Comment> comList = getFieldComment(tableName,conn);
        if(list!=null&&list.size()>0&&comList!=null&&comList.size()>0){
            for (TableDef def : list) {
                for (Comment com : comList) {
                    if(def.getBs().equals(com.getBs())){
                        String tmp = com.getComment();
                        if(!StringUtils.isEmpty(tmp)){

                            //先把中文括号转换为英文括号
                            if (tmp.contains("（")&& tmp.contains("）")){
                                tmp = tmp.replaceAll("（", "(").replaceAll("）", ")");
                            }
                            //再把字符串切割成两个，分别设置为名称和描述
                            int s=tmp.indexOf("("),d=tmp.indexOf(")");
                            if(s!=-1&&d!=-1){
                                def.setMc(tmp.substring(0, s));
                                def.setMs(tmp.substring(s+1, d));
                            }else{
                                def.setMc(tmp);
                            }
                        }
                        break;
                    }
                }
            }
        }
        return list;
    }

    //根据表格名称，获取字段详情
    public static List<Comment> getFieldComment(String tableName,Connection conn){
        List<Comment> list = new ArrayList<Comment>();
        String sql = MessageFormat.format("SELECT COLUMN_NAME, COMMENTS FROM user_col_comments WHERE TABLE_NAME = '{0}'", new String[] { tableName });
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet remarkSet = statement.executeQuery();
            while (remarkSet.next())
            {
                String colName = remarkSet.getString("COLUMN_NAME");
                String remark = remarkSet.getString("COMMENTS");
                Comment en = new Comment();
                en.setBs(colName);
                en.setComment(remark);
                list.add(en);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void main(String[] args) {
        String format = MessageFormat.format("SELECT COLUMN_NAME, COMMENTS FROM user_col_comments WHERE TABLE_NAME = '{0}'", new String[]{"aaa"});
        System.out.println(format);
    }
}

