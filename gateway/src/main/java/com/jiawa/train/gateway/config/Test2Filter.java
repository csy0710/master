//package com.jiawa.train.gateway.config;
//
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.Ordered;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//@Component
////实现tokne校验拦截器
//public class Test2Filter implements GlobalFilter , Ordered {//Ordered用于决定过滤器使用顺序
//    private static final Logger LOG = LoggerFactory.getLogger(Test2Filter.class);//声明日志
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        LOG.info("Test2Filter");
//        return chain.filter(exchange);//这个过滤器走完走下一个过滤器
//    }
//
//    @Override
//    public int getOrder() {//Ordered用于决定过滤器使用顺序
//        return 0;
//    }
//}
