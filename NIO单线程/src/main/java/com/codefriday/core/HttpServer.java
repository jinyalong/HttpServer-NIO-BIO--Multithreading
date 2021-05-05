package com.codefriday.core;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author codefriday
 * @data 2021/4/9
 */
public class HttpServer {
    protected static final String WEB_ROOT=System.getProperty("user.dir")+ File.separator+"webroot";
    private int PORT = 8000;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    public void run(){
        if(serverSocketChannel == null){
            System.out.println("服务器启动中~");
            System.out.println(System.getProperty("user.dir"));
            init();
        }
        while(true){
            try {
                this.selector.select();
                Set<SelectionKey> selectionKeys = this.selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while(iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    doHandler(selectionKey);
                    iterator.remove();
                }
            }catch (Exception e){
                e.printStackTrace();
                continue;
            }
        }
    }
    private void init(){
        try {
            // 获得serverSocketChannel
            this.serverSocketChannel = ServerSocketChannel.open();
            this.serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
            //设置为非阻塞
            this.serverSocketChannel.configureBlocking(false);
            this.selector = Selector.open();
            //负责接收连接
            this.serverSocketChannel.register(this.selector , SelectionKey.OP_ACCEPT);
        } catch (Exception e){

        }
    }
    private void doHandler(SelectionKey selectionKey) throws Exception {
        //如果是一个接入连接
        if(selectionKey.isAcceptable()){
            System.out.println("====新链接！");
            this.doHandlerLink();
        }else if(selectionKey.isReadable()){
            SocketChannel socketChannel = (SocketChannel)selectionKey.channel();
            try {
                doHandlerAccess(socketChannel);
            }catch (Exception e){
                socketChannel.close();
                selectionKey.cancel();
                throw e;
            }
        }
    }

    private void doHandlerLink() throws IOException {
        SocketChannel newClient = this.serverSocketChannel.accept();
        newClient.configureBlocking(false);
        newClient.register(selector,SelectionKey.OP_READ);
    }

    private void doHandlerAccess(SocketChannel socketChannel) throws Exception {
        //解析请求
        Request request = doHandleRequest(socketChannel);
        //回送客户端
        doHandleReply(request, socketChannel);
    }

    private void doHandleReply(Request request, SocketChannel socketChannel) throws Exception {
        Response response=new Response(socketChannel,request);
        response.sendStaticResource();
    }

    //处理到来的request请求
    private Request  doHandleRequest(SocketChannel socketChannel) throws Exception {
        Request request=new Request(socketChannel);
        request.doHandelRequestContext();
        return  request;
    }

}
