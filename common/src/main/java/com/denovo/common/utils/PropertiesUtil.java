package com.denovo.common.utils;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * @author liusinan
 * @version 1.0.0
 * @ClassName PropertiesUtil.java
 * @Description TODO
 * @createTime 2021年07月02日 10:03:00
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class PropertiesUtil {

    private static FileUtil util;

    public PropertiesUtil() {
        util = new FileUtil();
    }

    public PropertiesUtil(FileUtil fileUtil) {
        this.util = fileUtil;
    }

    /*根据相对路径获得properties文件*/
    public static Properties getProperties(String relativePath) throws IOException {
        Properties prop = new Properties();
        prop.load(new FileUtil().getRelativeBuffer(relativePath));
        return prop;
    }

    @Test
    void main() throws IOException {
        Properties properties = getProperties("resources/pulsar.properties");
        System.out.println(properties.stringPropertyNames());
    }
}
