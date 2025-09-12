package com.jiawa.train.batch.job;

import cn.hutool.core.util.RandomUtil;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@DisallowConcurrentExecution  //禁止并发执行
public class DailyTrainJob implements Job {
public static final Logger LOG = LoggerFactory.getLogger(DailyTrainJob.class);
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 增加日志流水号
        MDC.put("LOG_ID", System.currentTimeMillis() + RandomUtil.randomString(3));
        LOG.info("生成每日车次数据 开始");
        LOG.info("生成每日车次数据 结束");
    }
}
