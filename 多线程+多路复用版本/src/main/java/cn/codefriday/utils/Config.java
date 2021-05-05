package cn.codefriday.utils;

import java.net.URL;

/**
 * @author codefriday
 * @data 2021/4/16
 */
public class Config {
    public static int PORT = 8001;
    public static String WEBROOT;

    static {
        WEBROOT = System.getProperty("user.dir")+ "\\webroot";
        System.out.println(WEBROOT);
    }
}
