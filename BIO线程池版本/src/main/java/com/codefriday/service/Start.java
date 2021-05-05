package com.codefriday.service;


/**
 * @author codefriday
 * @data 2021/3/23
 * @description 启动服务器
 */
public class Start {
    public static void main(String[] args) {
        new Thread(new RequestInterception()).start();
    }
}
