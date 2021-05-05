package com.codefriday.core;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author codefriday
 * @data 2021/4/9
 */
public class Request {
    private String requestContext;
    private SocketChannel socketChannel;
    private String url;

    public Request(SocketChannel socketChannel){
        this.socketChannel=socketChannel;
    }

    public String getRequestContext() {
        return requestContext;
    }
    public String getUrl() {
        return url;
    }
    void doHandelRequestContext() throws Exception{
        parseRequestContext();
        parseRequestUrl();
    }

    private void parseRequestContext() throws Exception {
        ByteBuffer byteBuffer=ByteBuffer.allocate(102400);
        byteBuffer.clear();
        int length=0;
        StringBuilder stringBuilder=new StringBuilder();
        while ((length = socketChannel.read(byteBuffer)) > 0) {
            stringBuilder.append(new String(byteBuffer.array(), 0, length));
        }
        this.requestContext = stringBuilder.toString();
        if (this.requestContext.trim().equals("")) {
            throw new Exception("请求不合法");
        }
    }

    private void parseRequestUrl(){
        int index=requestContext.indexOf(" ");
        if (index!=-1){
            url=requestContext.substring(index+1,requestContext.indexOf(" ",index+1));
        }
        //默认请求/index.html
        if(url.equals("/")){
            url="/index.html";
        }
    }
}
