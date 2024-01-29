package com.example.tool.exportData.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.example.tool.common.utils.FileUtils;
import com.example.tool.common.utils.StringUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.util.StringUtil;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;

/**
 * word工具
 * @author Administrator
 *
 */
public class WordUtils {

    /**
     * word模板替换
     * @param templatePath
     * @param outPath
     * @param map
     * @return
     */
    public static boolean replaceWord(String templatePath, String outPath, Map<String, Object> map){
        String t1 = getFileType(FileUtils.getFile(templatePath));
        String t2 = getFileType(outPath);
        //创建目录
        File file = new File(outPath);
        File pfile= new File(file.getParentFile().getPath());
        if(!pfile.exists()) pfile.mkdirs();
        //根据文档类型替换模板内容
        if(t1.equalsIgnoreCase("doc")&&t2.equalsIgnoreCase("doc")){
            return replacedoc(templatePath,outPath,map);
        }else if(t1.equalsIgnoreCase("docx")&&t2.equalsIgnoreCase("docx")){
            return replacedocx(templatePath,outPath,map);
        }else{
            return false;
        }
    }

    /**
     * doc
     * @param templateFile
     * @param outFile
     * @param map
     * @return
     */
    private static boolean replacedoc(String templateFile, String outFile, Map<String, Object> map) {
        FileOutputStream fos = null;
        try {
            HWPFDocument document = new HWPFDocument(new FileInputStream(templateFile));
            Range range = document.getRange();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                range.replaceText(entry.getKey(), entry.getValue()==null?"":entry.getValue().toString());
            }
            fos = new FileOutputStream(outFile);
            document.write(fos);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally{
            if(fos!=null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * docx
     * @param templatePath
     * @param destPath
     * @param map
     * @return
     */
    private static boolean replacedocx(String templatePath, String outPath, Map<String, Object> map) {
        FileOutputStream fos = null;
        XWPFDocument document = null;
        try {
            //XWPFDocument document = new XWPFDocument(POIXMLDocument.openPackage(templatePath));
            document = new XWPFDocument(FileUtils.getFileInputStream(templatePath));
            // 替换段落中的指定文字
            List<XWPFParagraph> paraList = document.getParagraphs();
            processParagraphs(paraList,map,document);

            // 替换表格中的指定文字
            Iterator<XWPFTable> itTable = document.getTablesIterator();
            while (itTable.hasNext()) {
                XWPFTable table = (XWPFTable) itTable.next();
                int rcount = table.getNumberOfRows();
                for (int i = 0; i < rcount; i++) {
                    XWPFTableRow row = table.getRow(i);
                    List<XWPFTableCell> cells = row.getTableCells();
                    for (XWPFTableCell cell : cells) {
                        paraList= cell.getParagraphs();
                        processParagraphs(paraList,map,document);
                    }
                }
            }
            fos = new FileOutputStream(outPath);
            document.enforceReadonlyProtection("83849888", HashAlgorithm.md5);
            document.write(fos);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally{
            try {
                if(fos!=null) fos.close();
                if(document!=null) document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //处理段落的替�?
    public static void processParagraphs(List<XWPFParagraph> paraList,Map<String, Object> param,XWPFDocument doc){
        if(paraList!=null&&paraList.size()>0){
            for (XWPFParagraph para : paraList) {
                XWPFParagraph paragraph = (XWPFParagraph) para;
                List<XWPFRun> runs = paragraph.getRuns();
                for (int j = 0; j < runs.size(); j++) {
                    boolean bReplace = false;
                    String oneparaString = runs.get(j).getText(runs.get(j).getTextPosition());
                    for (Map.Entry<String, Object> entry : param.entrySet()) {
                        if(!StringUtils.isEmpty(oneparaString)){
                            if(oneparaString.indexOf(entry.getKey())!=-1){//如果包括替换文本
                                bReplace = true;
                                Object value = entry.getValue();
                                if(value==null){
                                    oneparaString = oneparaString.replace(entry.getKey(), "");
                                }else if(value instanceof String){
                                    oneparaString = oneparaString.replace(entry.getKey(), entry.getValue().toString());
                                }else if(value instanceof Map){
                                    //图片处理
                                    oneparaString = oneparaString.replace(entry.getKey(), "");
                                    @SuppressWarnings("unchecked")
                                    Map<String,Object> pic = (Map<String,Object>)value;
                                    Integer width = Integer.parseInt(pic.get("width").toString());
                                    Integer height = Integer.parseInt(pic.get("height").toString());
                                    int picType = getPictureType(pic.get("type").toString());
                                    byte[] byteArray = (byte[]) pic.get("content");
                                    if(byteArray!=null&&byteArray.length>0){
                                        ByteArrayInputStream pictureData = null;
                                        try {
                                            bReplace = false;
                                            runs.get(j).setText("", 0);
                                            pictureData = new ByteArrayInputStream(byteArray);
                                            runs.get(j).addPicture(pictureData, picType, "", Units.toEMU(width), Units.toEMU(height));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }finally{
                                            try {
                                                if(pictureData!=null) pictureData.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                    if(bReplace) runs.get(j).setText(oneparaString, 0);;
                }
            }
        }
    }

    /**
     * 根据图片类型，取得对应的图片类型代码
     * @param picType
     * @return int
     */
    private static int getPictureType(String picType){
        int format = -1;
        if(picType.equalsIgnoreCase("emf")) format = XWPFDocument.PICTURE_TYPE_EMF;
        else if(picType.equalsIgnoreCase("wmf")) format = XWPFDocument.PICTURE_TYPE_WMF;
        else if(picType.equalsIgnoreCase("pict")) format = XWPFDocument.PICTURE_TYPE_PICT;
        else if(picType.equalsIgnoreCase("jpeg") || picType.equalsIgnoreCase("jpg")) format = XWPFDocument.PICTURE_TYPE_JPEG;
        else if(picType.equalsIgnoreCase("png")) format = XWPFDocument.PICTURE_TYPE_PNG;
        else if(picType.equalsIgnoreCase("dib")) format = XWPFDocument.PICTURE_TYPE_DIB;
        else if(picType.equalsIgnoreCase("gif")) format = XWPFDocument.PICTURE_TYPE_GIF;
        else if(picType.equalsIgnoreCase("tiff")) format = XWPFDocument.PICTURE_TYPE_TIFF;
        else if(picType.equalsIgnoreCase("eps")) format = XWPFDocument.PICTURE_TYPE_EPS;
        else if(picType.equalsIgnoreCase("bmp")) format = XWPFDocument.PICTURE_TYPE_BMP;
        else if(picType.equalsIgnoreCase("wpg")) format = XWPFDocument.PICTURE_TYPE_WPG;
        else {
            System.err.println("Unsupported picture: " + picType +". Expected emf|wmf|pict|jpeg|png|dib|gif|tiff|eps|bmp|wpg");
        }
        return format;
    }



    /**
     * 获取
     * @param filepath
     * @return
     */
    public static String getFileType(String filepath){
        String fileType = "";
        if(filepath!=null){
            File file = new File(filepath);
            String fileName = file.getName();
            int index = fileName.lastIndexOf(".");
            if(index>=0){
                fileType = fileName.substring(index+1,fileName.length());
            }
        }
        return fileType;
    }
    /**
     * 获取
     * @param filepath
     * @return
     */
    public static String getFileType(File file){
        String fileType = "";
        String fileName = file.getName();
        int index = fileName.lastIndexOf(".");
        if(index>=0){
            fileType = fileName.substring(index+1,fileName.length());
        }
        return fileType;
    }
    public static void main(String[] args) throws Exception {
        // 创建新的空白Word文档作为目标文件
        XWPFDocument mergedDoc = new XWPFDocument();

        // 定义需要合并的源文件路径列表
//        String[] sourceFiles = {"001a.docx", "001b.docx", "001c.docx"};
        String prefix = "D:\\template\\db\\生成时间20240129112647//";
        File fold = new File(prefix);
        File[] fs = fold.listFiles();
        for(File file:fs){
            FileInputStream fis = new FileInputStream(file);
            // 读取每个源文件中的内容
            XWPFDocument doc = new XWPFDocument(fis);
            copyElements(doc, mergedDoc);
            // 关闭输入流
            fis.close();
        }

        // 保存合并后的文档
        FileOutputStream fos = new FileOutputStream(prefix + "\\000merged.docx");
        mergedDoc.write(fos);
        fos.close();

        System.out.println("合并完成！");
    }
    public static void copyElements(XWPFDocument sourceDocument, XWPFDocument targetDocument) {
        List<IBodyElement> elements = sourceDocument.getBodyElements();
        for (IBodyElement element : elements) {
            if (element instanceof XWPFParagraph) {
                XWPFParagraph paragraph = (XWPFParagraph) element;
                copyParagraph(paragraph, targetDocument.createParagraph());
            } else if (element instanceof XWPFTable) {
                XWPFTable table = (XWPFTable) element;
                copyTable(table, targetDocument.createTable());
            }
        }
    }
    private static void copyTable(XWPFTable source, XWPFTable target) {
        target.getCTTbl().setTblPr(source.getCTTbl().getTblPr());
        target.getCTTbl().setTblGrid(source.getCTTbl().getTblGrid());
        for (int r = 0; r<source.getRows().size(); r++) {
            XWPFTableRow targetRow = target.createRow();
            XWPFTableRow row = source.getRows().get(r);
            targetRow.getCtRow().setTrPr(row.getCtRow().getTrPr());
            for (int c=0; c<row.getTableCells().size(); c++) {
                //newly created row has 1 cell
                XWPFTableCell targetCell = c==0 ? targetRow.getTableCells().get(0) : targetRow.createCell();
                XWPFTableCell cell = row.getTableCells().get(c);
                targetCell.getCTTc().setTcPr(cell.getCTTc().getTcPr());
                XmlCursor cursor = targetCell.getParagraphArray(0).getCTP().newCursor();
                for (int p = 0; p < cell.getBodyElements().size(); p++) {
                    IBodyElement elem = cell.getBodyElements().get(p);
                    if (elem instanceof XWPFParagraph) {
                        XWPFParagraph targetPar = targetCell.insertNewParagraph(cursor);
                        cursor.toNextToken();
                        XWPFParagraph par = (XWPFParagraph) elem;
                        copyParagraph(par, targetPar);
                    } else if (elem instanceof XWPFTable) {
                        XWPFTable targetTable = targetCell.insertNewTbl(cursor);
                        XWPFTable table = (XWPFTable) elem;
                        copyTable(table, targetTable);
                        cursor.toNextToken();
                    }
                }
                //newly created cell has one default paragraph we need to remove
                targetCell.removeParagraph(targetCell.getParagraphs().size()-1);
            }
        }
        //newly created table has one row by default. we need to remove the default row.
        target.removeRow(0);
    }
    private static void copyParagraph(XWPFParagraph source, XWPFParagraph target) {
        target.getCTP().setPPr(source.getCTP().getPPr());
        for (int i=0; i<source.getRuns().size(); i++ ) {
            XWPFRun run = source.getRuns().get(i);
            XWPFRun targetRun = target.createRun();
            //copy formatting
            targetRun.getCTR().setRPr(run.getCTR().getRPr());
            //no images just copy text
            targetRun.setText(run.getText(0));
        }
    }
}
