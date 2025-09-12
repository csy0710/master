package com.jiawa.train.batch.job;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component           //增加注解，springBoot才能扫描到这个类
@EnableScheduling    //开启定时任务
public class SpringBootTestJob {
    @Scheduled(cron = "0/5 * * * * ?")//cron表达式，表示每五秒执行一次
        private void test(){
            System.out.println("SpringBootTestJob Test");
        }
}
