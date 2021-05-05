package cn.codefriday.cores;

import cn.codefriday.utils.Config;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author codefriday
 * @data 2021/4/16
 */
@Slf4j
public class BootStrap {
    private ServerSocketChannel ssc;
    private Selector selector;
    private EventLoopWorker []workers;
    private AtomicInteger index = new AtomicInteger();
    private int cores;
    // 初始化
    public BootStrap(){
        try {
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            selector = Selector.open();
            ssc.register(selector , SelectionKey.OP_ACCEPT);
            ssc.bind(new InetSocketAddress(Config.PORT));
            log.debug("服务器启动~监听端口【{}】",Config.PORT);
            cores = Runtime.getRuntime().availableProcessors();
            workers = new EventLoopWorker[cores];
            for(int i = 0;i < cores;i++){
                workers[i] = new EventLoopWorker("worker-"+i);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("服务器监听端口创建失败！");
        }
    }

    public void run(){
        while (true){
            try {
                selector.select();
                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext()){
                    SelectionKey key = iter.next();
                    iter.remove();
                    SocketChannel sc = ssc.accept();
                    log.debug("connected...");
                    workers[index.getAndIncrement() % cores].registerClient(sc);
                    //log.debug("after register...");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new BootStrap().run();
    }

}
