package com.example.tool.exportData.execute;

import com.example.tool.common.utils.FileUtils;
import com.example.tool.common.utils.StringUtils;
import com.example.tool.exportData.model.Model;
import com.example.tool.exportData.model.TableDef;
import com.example.tool.exportData.utils.GetMysqlComment;
import com.example.tool.exportData.utils.GetOracleComment;
import com.example.tool.exportData.utils.WordUtils;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class JdbcTest {

    /**
     * @Desc 方法入口
     * @Desc 配置好sql.properties文件后，直接运行main方法即可完成生成。
     */
    public static void main(String[] args) throws Exception {
        //生成表格名称列表和表格功能列表，保存于"tb_mb.docx"路径下的文件中，并对每个表生成数据库表格详情列表，储存到target文件夹中
        exportTable();
    }

    private static String driver = null;
    private static String url = null;
    private static String user = null;
    private static String pwd = null;
    private static Connection conn = null;
    private static String target = null;
    //修改两个模板文件的位置，需修改下面的路径。 建议放在同级目录。
    private static String word_mb = "/template/tb_mb.docx";
    private static String tb_mb = "/template/m.docx";
    private static String dbName = null;
    private static String sqlLx = null;
    private static String curentTime="";

    static {
        //获取当前时间加到文件名上
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        curentTime = simpleDateFormat.format(date);
        //获取配置文件中相应的参数,并利用参数获取数据库连接
        Properties property = new Properties();
        try {
            //修改配置文件（sql.properties）的位置，需修改下面的路径。
            property.load(FileUtils.getFileInputStream("/sql.properties"));
            sqlLx = property.getProperty("sqlLx");
            driver = property.getProperty("driver");
            url = property.getProperty("url");
            user = property.getProperty("user");
            pwd = property.getProperty("pwd");
            //每次生成都产生一个新的文件夹，名称加当前时间
            String tempDir = property.getProperty("target");
            if (tempDir==null||tempDir.length()==0){
                tempDir="C:\\";
            }
            target = ""+tempDir+"生成时间"+curentTime+"//";
            dbName = property.getProperty("dbName");
            if (StringUtils.isEmpty(driver)||StringUtils.isEmpty(url)||StringUtils.isEmpty(user)||StringUtils.isEmpty(pwd)){
                System.out.println("数据库连接参数非法。");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("获取配置文件失败。");
        }
        //连接数据库
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url,user,pwd);
        } catch (Exception e) {
            System.out.println("数据库连接异常");
            e.printStackTrace();
        }
    }

    //根据数据库表名称模糊查找表名和表注释
    private static void exportTable() throws Exception{
        List<Model> list = getTableComment(dbName,conn);
        if(list!=null&&list.size()>0){
            System.out.println("一共查到"+list.size()+"个表格。");
            fillDocxByTable(list);
            System.out.println("填充表名和表注释到doc文档成功。");
        }
        exportField(dbName, list);
        mergeWord();
        disConnect();
    }

    private static void mergeWord() throws IOException {
        XWPFDocument mergedDoc = new XWPFDocument();
        // 定义需要合并的源文件路径列表
//        String[] sourceFiles = {"001a.docx", "001b.docx", "001c.docx"};
        File fold = new File(target);
        File[] fs = fold.listFiles();
        for(File file:fs){
            FileInputStream fis = new FileInputStream(file);
            // 读取每个源文件中的内容
            XWPFDocument doc = new XWPFDocument(fis);
            WordUtils.copyElements(doc, mergedDoc);
            // 关闭输入流
            fis.close();
        }
        // 保存合并后的文档
        FileOutputStream fos = new FileOutputStream(target + "\\000"+dbName+"数据库结构表.docx");
        mergedDoc.write(fos);
        fos.close();
    }

    //从mysql的information_schema数据库中查询数据库的表格名称和注释
    public static List<Model> getTableComment(String table,Connection conn){
        List<Model> list = new ArrayList<Model>();
        if ("mysql".equalsIgnoreCase(sqlLx)){
            list = GetMysqlComment.getComment(table, conn);
        }else if ("oracle".equalsIgnoreCase(sqlLx)){
            list = GetOracleComment.getComment(table, conn);
        }
        return list;
    }

    //填充表名和表注释到doc文档中
    public static void fillDocxByTable(List<Model> list){
        Map<String, Object> params = new HashMap<String, Object>();
        File path = new File(target);
        if(!path.exists()) path.mkdirs();
        String fileName = target+"000数据库汇总表.docx";
        if (WordUtils.replaceWord(tb_mb, fileName, params)){
            int rowIndex = 1;// 表头1行，内容从第二行开始填充
            if (insertDocxRow(fileName, 0, list.size()-1, rowIndex)) {
                // 填充表名和表注释到doc文档中
                fillDocxTbData(fileName, list, rowIndex-1);
            }
        }

    }

    //填充序号、表名、表格功能到doc文档中
    private static boolean fillDocxTbData(String target, List<Model> list, Integer rowIndex) {
        XWPFDocument document = null;
        FileOutputStream fos = null;
        XWPFTable table = null;
        Iterator<XWPFTable> itTable = null;
        try {
            document = new XWPFDocument(new FileInputStream(target));
            fos = new FileOutputStream(target);
            itTable = document.getTablesIterator();
            table = (XWPFTable) itTable.next();
            int i = 1;
            XWPFTableRow temp = null;
            if (list != null && list.size() > 0) {
                for (Model tb : list) {
                    temp = table.getRow(rowIndex + i);
                    clearData(temp, 3);// 将temp中数据填充到3列中
                    temp.getCell(0).setText(String.valueOf(i));
                    temp.getCell(1).setText(tb.getTableName());
                    temp.getCell(2).setText(tb.getDesc());
                    i++;
                }
            }
            document.write(fos);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (fos != null)
                    fos.close();
                if (document != null)
                    document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    //根据表名模糊查询，获取字段详细信息
    private static void exportField(String dbname, List<Model> list) throws Exception{
        System.out.println("数据库中一共有"+list.size()+"个表格，将查找每个表格的详细信息并生成对应word文档。");
        if(list!=null&&list.size()>0){
            int docCount=1;
            if ("mysql".equalsIgnoreCase(sqlLx)){
                for (Model model : list) {
                    //model中有两个参数，getTableName()可获取字母表名，getDesc()可获取功能说明（eg:"重点国家表(系统默认&用户自定义)"）
                    List<TableDef> defList = GetMysqlComment.getTableDef(dbname, model.getTableName(), conn);
                    model.setDefList(defList);
                    fillDocx(model);
                    System.out.println("生成了第"+docCount+"个文档，名称是"+model.getTableName());
                    docCount++;
                }
            }else if ("oracle".equalsIgnoreCase(sqlLx)){
                for (Model model : list) {
                    //model中有两个参数，getTableName()可获取字母表名，getDesc()可获取功能说明（eg:"重点国家表(系统默认&用户自定义)"）
                    List<TableDef> defList = GetOracleComment.getTableDef(model.getTableName(), conn);
                    model.setDefList(defList);
                    fillDocx(model);
                    System.out.println("生成了第"+docCount+"个文档，名称是"+model.getTableName());
                    docCount++;
                }
            }
            System.out.println("所有的数据库表格详情文档生成完毕，程序运行结束。");
            System.out.println("请在 "+target+" 文件夹中查看生成的文件。");
        }
        disConnect();
    }

    //在word文档中添加对应行数的空白表格行
    private static boolean insertDocxRow(String target, Integer tableIndex, Integer rowNum, Integer rowIndex) {
        XWPFDocument document = null;
        FileOutputStream fos = null;
        XWPFTable table = null;
        Iterator<XWPFTable> itTable = null;
        if (tableIndex == null)
            tableIndex = 0;
        try {
            document = new XWPFDocument(new FileInputStream(target));
            fos = new FileOutputStream(target);
            itTable = document.getTablesIterator();
            int index = 0;
            while (itTable.hasNext()) {
                table = (XWPFTable) itTable.next();
                if (tableIndex.intValue() == index)
                    break;
                index++;
            }
            XWPFTableRow row = table.getRow(rowIndex);
            for (int i = 1; i <= rowNum; i++) {
                table.addRow(row, rowIndex);
            }
            document.write(fos);
        } catch (Exception e1) {
            e1.printStackTrace();
            return false;
        } finally {
            try {
                if (fos != null)
                    fos.close();
                if (document != null)
                    document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    //把各个表的具体信息填充到word文档中
    public static void fillDocx(Model model){
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("tableName", model.getTableName());
        params.put("desc", model.getDesc());
        File path = new File(target);
        if(!path.exists()) path.mkdirs();
        //生成文件名,把当前时间加到文件名上
        String fileName=target+model.getTableName()+".docx";
        if (WordUtils.replaceWord(word_mb, fileName, params)) {
            int rowIndex = 4;// 表头4行，内容从第5行开始填充
            if (insertDocxRow(fileName, 0, model.getDefList().size()-1, rowIndex)) {
                // 把各个表的具体信息填充到word文档中，失败就输出提示一下。
                boolean IsSuccess = fillDocxData(fileName, model.getDefList(), rowIndex - 1);
                if (!IsSuccess){
                    System.out.println(model.getTableName()+"生成失败。");
                }
            }
        }
    }

    //把各个表的具体信息填充到word文档中，返回true
    private static boolean fillDocxData(String target, List<TableDef> list, Integer rowIndex) {
        XWPFDocument document = null;
        FileOutputStream fos = null;
        XWPFTable table = null;
        Iterator<XWPFTable> itTable = null;
        try {
            document = new XWPFDocument(new FileInputStream(target));
            fos = new FileOutputStream(target);
            itTable = document.getTablesIterator();
            table = (XWPFTable) itTable.next();
            int i = 1;
            XWPFTableRow temp = null;
            if (list != null && list.size() > 0) {
                for (TableDef def : list) {
                    temp = table.getRow(rowIndex + i);
                    // 每个字段有8个属性，分别是序号、标识、字段类型、名称、描述、约束条件、关联表、是否主键
                    clearData(temp, 8);
                    temp.getCell(0).setText(String.valueOf(i));
                    temp.getCell(1).setText(def.getBs());
                    temp.getCell(2).setText(def.getLx());
                    temp.getCell(3).setText(def.getMc());
                    temp.getCell(4).setText(def.getMs());
                    temp.getCell(5).setText(def.getYstj());
                    temp.getCell(6).setText(def.getGlb());
                    temp.getCell(7).setText(def.getZj().equals("1")?"√":"");
                    i++;
                }
            }
            document.write(fos);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (fos != null)
                    fos.close();
                if (document != null)
                    document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    // 遍历获取数据
    private static void clearData(XWPFTableRow row, Integer cols) {
        for (int col = 0; col < cols; col++) {
            if (row.getCell(col).getParagraphs().size() > 0) {
                List<XWPFParagraph> plist = row.getCell(col).getParagraphs();
                for (XWPFParagraph xwpfParagraph : plist) {
                    List<XWPFRun> runs = xwpfParagraph.getRuns();
                    if (runs != null && runs.size() > 0) {
                        for (int j = 0; j < runs.size(); j++) {
                            xwpfParagraph.removeRun(j);
                        }
                    }
                }
            }
        }
    }

    //断开数据库连接
    public static void disConnect(){
        try {
            if(conn!=null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

