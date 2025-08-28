package com.jiawa.train.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

@SpringBootApplication
@ComponentScan("com.jiawa")
public class GatewayApplication {

    private static final Logger L0G = LoggerFactory.getLogger(GatewayApplication.class);
    //启动日志
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(GatewayApplication.class);
        Environment env = app.run(args).getEnvironment();//拿到整个项目运行的环境
        L0G.info("启动成功!!");
        L0G.info("网关地址:\thttp://127.0.0.1:{}", env.getProperty("server.port"));//通过环境获取系统的变量 server.port是启动端口
    }
}
