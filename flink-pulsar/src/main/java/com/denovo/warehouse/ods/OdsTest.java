package com.denovo.warehouse.ods;

import com.denovo.common.utils.PropertiesUtil;

import java.io.IOException;
import java.util.Properties;

/**
 * @author liusinan
 * @version 1.0.0
 * @ClassName OdsTest.java
 * @Description TODO
 * @createTime 2021年07月02日 10:24:00
 */
public class OdsTest {

    public static void main(String[] args) throws IOException {
        /*PropertiesUtil propertiesUtil = new PropertiesUtil();*/
        Properties properties = PropertiesUtil.getProperties("resources/pulsar.properties");
        System.out.println(properties.stringPropertyNames());
    }
}
