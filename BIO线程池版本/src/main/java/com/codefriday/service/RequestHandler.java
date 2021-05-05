package com.codefriday.service;

import com.codefriday.data.Contains;

import java.io.*;
import java.net.Socket;
import java.util.Date;

/**
 * @author codefriday
 * @data 2021/3/23
 * @description 接收请求获得socket并处理请求
 */
public class RequestHandler implements Runnable {
    private Socket socket;
    private String reqPath; //请求的路径
    private String host;    //请求的主机

    public RequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
            this.parseRequest(in);
            this.doResponse(out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                in.close();
                //socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //分析请求头方法
    private void parseRequest(InputStream in) throws IOException {
        if (in == null) return;
        InputStreamReader inputStreamReader = new InputStreamReader(in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        ;
        String line = null;
        int lineNum = 1;
        while ((line = bufferedReader.readLine()) != null) {
            if (lineNum == 1) {
                String[] infos = line.split(" ");
                if (infos != null || infos.length > 2) {
                    reqPath = infos[1];
                } else {
                    throw new RuntimeException("请求解析失败：" + line);
                }
            } else {
                String[] infos = line.split(": ");
                if (infos[0].equals("Host")) {
                    host = infos[1];
                }
            }
            if (line.equals("")) break;//读取到空行结束
            lineNum++;
        }

    }

    private void doResponse(OutputStream out) {
        if (out == null) return;
        //====响应请求====
        PrintWriter pw = new PrintWriter(out);
        pw.println("HTTP/1.1 200 OK");
        pw.println("Date: " + new Date().toString());
        pw.println(Contains.HTML);
        pw.println("");
        pw.println("<h1>欢迎访问CodeFriday的服务器！</h1>");
        pw.flush();
    }
}
