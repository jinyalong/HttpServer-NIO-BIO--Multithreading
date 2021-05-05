package com.codefriday.core;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * @author codefriday
 * @data 2021/4/9
 */
public class Response {
    private SocketChannel socketChannel;
    private Request request;

    public Response(SocketChannel socketChannel){
        this(socketChannel,null);
    }
    public Response(SocketChannel socketChannel,Request request){
        this.request=request;
        this.socketChannel=socketChannel;
    }
    public void setRequest(Request request) {
        this.request = request;
    }

    public void sendStaticResource() throws Exception {

        FileInputStream fileInputStream=null;
        File file =null;
        ByteBuffer byteBuffer=ByteBuffer.allocate(1024);
        try {
            file= new File(HttpServer.WEB_ROOT, request.getUrl());
            if (file.exists()) {
                fileInputStream = new FileInputStream(file);
                FileChannel fileChannel=fileInputStream.getChannel();
                byteBuffer.put(("HTTP/1.1 200\r\n"
                        + "Content-Type: text/html\r\n"
                        + "Content-Length: "
                        + file.length()
                        + "\r\n"
                        + "\r\n").getBytes());
                byteBuffer.flip();
                socketChannel.write(byteBuffer);
                byteBuffer.clear();
                fileChannel.transferTo(0,fileChannel.size(),socketChannel);
            } else {
                String errorMessage = "HTTP/1.1 404 File Not Found\r\n"
                        + "Content-Type:text/html\r\n"
                        + "Content-Length:23\r\n"
                        + "\r\n"
                        + "<h1>File Not Found</h1>";
                byteBuffer.put(errorMessage.getBytes());
                byteBuffer.flip();
                socketChannel.write(byteBuffer);
                byteBuffer.clear();
                throw new Exception("没找到指定文件");
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        finally {
            if(null!=fileInputStream){
                fileInputStream.close();
            }
        }
    }
}
