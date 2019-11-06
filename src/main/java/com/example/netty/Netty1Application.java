package com.example.netty;

import com.example.netty.common.Common;
import com.example.netty.handler.NettyServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Netty1Application {

    public static void main(String[] args) {
        SpringApplication.run(Netty1Application.class, args);

        try {
            new NettyServer(Common.WEB_SOCKET_PORT).start();
        }catch(Exception e) {
            System.out.println("NettyServerError:"+e.getMessage());
        }
    }

}
