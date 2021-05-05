package cn.codefriday.cores;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author codefriday
 * @data 2021/4/16
 */
@Slf4j
public class EventLoopWorker implements Runnable{
    private Thread thread;
    private Selector selector;
    private String name;
    private ConcurrentLinkedQueue<Runnable> clq = new ConcurrentLinkedQueue<>();

    public EventLoopWorker(String name){
        this.name = name;
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("["+this.name+"]EventLoopWorker创建selector失败");
        }
        log.debug("{}EventLoopWorker 创建线程！",this.name);
        thread = new Thread(this,this.name);
        thread.start();
    }
    @Override
    public void run() {
        while(true){
            try {
                selector.select();
                Runnable poll = clq.poll();
                if(poll!=null){
                    poll.run();
                }
                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext()){
                    SelectionKey key = iter.next();
                    iter.remove();
                    //log.debug("before read...");
                    new HttpRequest(key);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    public void registerClient(SocketChannel sc) throws IOException {
        sc.configureBlocking(false);
        clq.add(()->{
            try {
                sc.register(selector , SelectionKey.OP_READ);
                log.debug("{}注册成功",selector);
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            }
        });
        selector.wakeup();
    }
}
