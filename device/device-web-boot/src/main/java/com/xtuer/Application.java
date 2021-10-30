package com.xtuer;

import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.xtuer.ws.WsServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableMethodCache(basePackages = "com.xtuer.service2")
@EnableCreateCacheAnnotation
@MapperScan({ "com.xtuer.mapper" })
public class Application implements ApplicationRunner {
    @Autowired
    private WsServer wsServer;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * 启动 Websocket 服务
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        wsServer.start();
    }
}
