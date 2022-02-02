package com.denovo.common.utils;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liusinan
 * @version 1.0.0
 * @ClassName FileUtil.java
 * @Description TODO
 * @createTime 2021年07月02日 10:03:00
 */
public class FileUtil {

    /*获得文件内容消除换行，是否消除换行*/
    public String getFileContent(String path, boolean cleanLineCode) {
        StringBuilder sb = new StringBuilder();
        File file = new File(path);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String tmp;
            if (cleanLineCode)
                while ((tmp = br.readLine()) != null) {
                    sb.append(tmp);
                }
            else
                while ((tmp = br.readLine()) != null) {
                    sb.append(tmp).append("\n");
                }
            br.close();
        } catch (FileNotFoundException e) {
            System.out.println(String.format("找不到文件：%s", path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /*获得文件夹下所有文件内容*/
    public String getDirAllFileContent(String dir) {
        File dirFile = new File(dir);
        File[] files = dirFile.listFiles();
        if (files == null) return null;
        StringBuilder sb = new StringBuilder();
        for (File file : files) {
            if (file.isDirectory()) continue;
            sb.append(getFileContent(file.getAbsolutePath(), false));
        }
        return sb.toString();
    }

    /*将String写出到磁盘*/
    public void writeString(String data, String path) {
        File file = new File(path);
//        data = data.replaceAll("\\s+", "\n");
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(data.getBytes());
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*将数组内容写出磁盘*/
    public void writeNums(int[] nums, String path) {
        StringBuilder sb = new StringBuilder();
        for (int num : nums)
            sb.append(num).append("\n");
        writeString(sb.toString(), path);
    }

    /*获得相对路径的文件输入流*/
    public InputStream getRelativePath(String relativePath) {
        return this.getClass().getClassLoader().getResourceAsStream(relativePath);
    }

    /*获得相对路径的文件输入缓冲*/
    public BufferedReader getRelativeBuffer(String relativePath) {
        InputStream is = getRelativePath(relativePath);
        return new BufferedReader(new InputStreamReader(is));
    }

    /*获得相对路径文件内容*/
    public String getRelativeContent(String relativePath) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(getRelativePath(relativePath)));
        StringBuilder sb = new StringBuilder();
        String tmp;
        while ((tmp = br.readLine()) != null) {
            sb.append(tmp).append("\n");
        }
        return sb.toString();
    }

    /*根据文件夹路径判断文件夹是否已创建，并对为创建的文件夹进行创建*/
    public void createFilePath(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("file no exit");
            file.mkdirs();
        }
    }

    /*解码*/
    public String getFileContentGBK(String path) throws IOException {
        InputStream is = getRelativePath(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String tmp;
        StringBuilder sb = new StringBuilder();
        while ((tmp = br.readLine()) != null) {
            sb.append(tmp).append("\n");
        }
        return sb.toString();
    }

    /*相对文件路径获得字节数组*/
    public byte[] getFileContent(String relativePath) throws IOException {
        InputStream is = getRelativePath(relativePath);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] bytes = new byte[10];
        int len;
        while ((len = is.read(bytes)) != -1) {
            bos.write(bytes, 0, len);
        }
        return bos.toByteArray();
    }
}
