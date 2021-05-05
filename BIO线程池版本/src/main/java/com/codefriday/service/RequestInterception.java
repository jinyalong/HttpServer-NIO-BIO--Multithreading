package com.codefriday.service;

import com.codefriday.data.Configs;
import com.codefriday.data.Data;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author codefriday
 * @data 2021/3/23
 * @description 拦截来自浏览器的HTTP请求，得到响应的socket
 */
public class RequestInterception implements Runnable {
    private ExecutorService pools;

    public RequestInterception() {
        pools = new ThreadPoolExecutor(8, 8, 200, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        int cnt = 0;
        //从配置文件获得端口并监听该端口
        try {
            serverSocket = new ServerSocket(Configs.getPort());
            System.out.println("[INFO]开始监听端口：" + Configs.getPort());
            while (Data.isRun) {
                Socket socket = serverSocket.accept();
                cnt++;
                System.out.println("[INFO]接收到请求:" + cnt);
                RequestHandler handler = new RequestHandler(socket);
                pools.execute(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("服务器监听端口失败！");
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
