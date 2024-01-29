package com.example.tool.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class FileUtils {
    public static String getFilePath(String filePath){
        String path = FileUtils.class.getResource(filePath).getPath();
        return path;
    }
    public static File getFile(String filePath){
        String path = FileUtils.class.getResource(filePath).getPath();
        File file = new File(path);
        return file;
    }
    public static InputStream getFileInputStream(String filePath){
        InputStream inputStream = FileUtils.class.getResourceAsStream(filePath);
        return inputStream;
    }
    public static void main(String[] args) {
        String filePath = "/template/m.docx";
        String path = FileUtils.class.getResource(filePath).getPath();
        System.out.println(path);
        File file = new File(path);
        System.out.println(file.exists());
    }
}
