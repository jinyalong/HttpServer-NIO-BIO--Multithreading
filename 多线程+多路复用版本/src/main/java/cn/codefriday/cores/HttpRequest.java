package cn.codefriday.cores;

import cn.codefriday.utils.Config;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

/**
 * @author codefriday
 * @data 2021/4/16
 */
@Slf4j
public class HttpRequest {
    private SelectionKey key;
    private String METHOD;
    private String URL;
    private String HTTP_VERSION;
    private String KEEP_ALIVE;
    private String HOST;
    public HttpRequest(SelectionKey key){
        this.key = key;
        parseHttpRequest();
    }
    private void parseHttpRequest(){
        StringBuilder sb = new StringBuilder();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.clear();
        int length = 0;
        //拿到channel
        SocketChannel sc = (SocketChannel) key.channel();
        try {
            while ((length = sc.read(buffer)) > 0){
                sb.append(new String(buffer.array() , 0 ,length));
            }
            if(length == -1) key.cancel();
        } catch (IOException e) {
            e.printStackTrace();
            key.cancel();
        }
        //解析出的请求体
        String msg = sb.toString();
        String data = null;
        if(!msg.trim().equals("")){
            String[] split = msg.split("\r\n");
            String[] head = split[0].split(" ");
            METHOD = head[0];
            URL = head[1];
            HTTP_VERSION = head[2];
            HashMap<String,String> body = new HashMap<>();
            for(int i = 1;i < split.length;i++){
                if(i == split.length-1) {
                    data = split[i];
                }
                String[] split1 = split[i].split(": ");
                if(split1.length==2)
                    body.put(split1[0],split1[1]);
            }
            KEEP_ALIVE = body.get("Connection");
            HOST = body.get("Host");
            ByteBuffer byteBuffer=ByteBuffer.allocate(1024);
            if(METHOD.equals("POST")){
                log.debug("处理了POST请求----来自{}",sc);
                if(this.URL.equals("/Post_show")){
                    if(body.get("Content-Type").equals("application/x-www-form-urlencoded")){
                        HashMap<String,String> form_data = new HashMap<>();
                        if(data!=null){
                            String[] split1 = data.split("&");
                            for (String s : split1) {
                                String[] split2 = s.split("=");
                                if(split2.length==2) form_data.put(split2[0],split2[1]);
                            }
                            if(form_data.get("Name").equals("HNU")&&form_data.get("ID").equals("CS06142")){
                                do_post(byteBuffer,sc);
                            }else {
                                do_404(byteBuffer,sc);
                            }
                        }
                    }else{
                        log.debug("post请求提交数据格式错误！");
                    }
                }else{//404
                    do_404(byteBuffer,sc);
                }


            }else if(METHOD.equals("GET")){
                log.debug("处理了GET请求----来自{}",sc);
                if(URL.charAt(this.URL.length()-1)=='/'){
                    this.URL += "index.html";
                }
                File file =new File(Config.WEBROOT+this.URL);
                if(file.exists()){
                    try {
                        byteBuffer.put(("HTTP/1.1 200 OK\r\n"
                                + "Content-Type: text/html\r\n"
                                + "Content-Length: "
                                + file.length()
                                + "\r\n"
                                + "\r\n").getBytes());
                        byteBuffer.flip();
                        sc.write(byteBuffer);
                        byteBuffer.clear();
                        FileChannel channel = new FileInputStream(file).getChannel();
                        channel.transferTo(0,channel.size(),sc);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{//404
                   do_404(byteBuffer,sc);
                }
            }else{//不接受其他请求
                do_501(byteBuffer,sc);
            }

        }else {
            log.debug("请求无效来自-{}",sc);
        }

    }
    private void do_404(ByteBuffer byteBuffer,SocketChannel sc){
        try {
            File html_404 = new File(Config.WEBROOT+"\\404.html");
            byteBuffer.put(("HTTP/1.1 404 Not Found\r\n"
                    + "Content-Type: text/html\r\n"
                    + "Content-Length: "
                    + html_404.length()
                    + "\r\n"
                    + "\r\n").getBytes());
            byteBuffer.flip();
            sc.write(byteBuffer);
            byteBuffer.clear();
            FileChannel channel = new FileInputStream(html_404).getChannel();
            channel.transferTo(0,channel.size(),sc);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void do_501(ByteBuffer byteBuffer,SocketChannel sc){
        try {
            File html_501 = new File(Config.WEBROOT+"\\501.html");
            byteBuffer.put(("HTTP/1.1 501 Not Implemented\r\n"
                    + "Content-Type: text/html\r\n"
                    + "Content-Length: "
                    + html_501.length()
                    + "\r\n"
                    + "\r\n").getBytes());
            byteBuffer.flip();
            sc.write(byteBuffer);
            byteBuffer.clear();
            FileChannel channel = new FileInputStream(html_501).getChannel();
            channel.transferTo(0,channel.size(),sc);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void do_post(ByteBuffer byteBuffer,SocketChannel sc){
        try {
            File html_post = new File(Config.WEBROOT+"\\post_success.html");
            byteBuffer.put(("HTTP/1.1 200 OK\r\n"
                    + "Content-Type: text/html\r\n"
                    + "Content-Length: "
                    + html_post.length()
                    + "\r\n"
                    + "\r\n").getBytes());
            byteBuffer.flip();
            sc.write(byteBuffer);
            byteBuffer.clear();
            FileChannel channel = new FileInputStream(html_post).getChannel();
            channel.transferTo(0,channel.size(),sc);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
