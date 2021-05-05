package com.codefriday.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author codefriday
 * @data 2021/3/23
 * @description 读取配置文件
 */
public class Configs {
    private static String port;
    private static String resourcePath;

    //加载类时初始化
    static {
        InputStream is = null;
        Properties properties = new Properties();
        try {
            is = Configs.class.getClassLoader().getResourceAsStream("config.properties");
            properties.load(is);
            port = properties.getProperty("server_port");
            resourcePath = properties.getProperty("resource_path");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("加载服务器配置出错！");
        }

    }

    public static int getPort() {
        return Integer.parseInt(port);
    }

    public static String getResourcePath() {
        return resourcePath;
    }
}
