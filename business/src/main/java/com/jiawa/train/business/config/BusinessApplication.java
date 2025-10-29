package com.jiawa.train.business.config;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

@SpringBootApplication
@ComponentScan("com.jiawa")
@MapperScan("com.jiawa.train.*.mapper")
@EnableFeignClients("com.jiawa.train.business.feign")
//开启springboot缓存功能
@EnableCaching
public class BusinessApplication {

    private static final Logger L0G = LoggerFactory.getLogger(BusinessApplication.class);
    //启动日志
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BusinessApplication.class);
        Environment env = app.run(args).getEnvironment();//拿到整个项目运行的环境
        L0G.info("启动成功!!");
        L0G.info("测试地址:\thttp://127.0.0.1:{}{}/hello", env.getProperty("server.port"),env.getProperty("server.servlet.context-path"));//通过环境获取系统的变量 server.port是启动端口
    }
}
