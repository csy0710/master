package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.domain.Train;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.business.domain.DailyTrain;
import com.jiawa.train.business.domain.DailyTrainExample;
import com.jiawa.train.business.mapper.DailyTrainMapper;
import com.jiawa.train.business.req.DailyTrainQueryReq;
import com.jiawa.train.business.req.DailyTrainSaveReq;
import com.jiawa.train.business.resp.DailyTrainQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class DailyTrainService {
      public static final Logger LOG = LoggerFactory.getLogger(DailyTrainService.class);
    @Resource
    private DailyTrainMapper dailyTrainMapper;
    @Resource
    private TrainService trainService;
    @Resource
    private DailyTrainStationService dailyTrainStationService;
    @Resource
    private DailyTrainCarriageService dailyTrainCarriageService;
    public void save(DailyTrainSaveReq req){
        DateTime now = DateTime.now();
        // 将请求对象req的属性复制到DailyTrain对象中（需要确保两个类的属性名和类型匹配）
        DailyTrain dailyTrain = BeanUtil.copyProperties(req, DailyTrain.class);
        if (ObjectUtil.isNull(dailyTrain.getId())){/*根据id判断是新增保存还是编辑保存*/
            /*新增保存*/
            dailyTrain.setId(SnowUtil.getSnowflakeNextId());
            dailyTrain.setCreateTime(now);
            dailyTrain.setUpdateTime(now);
            dailyTrainMapper.insert(dailyTrain);
        }else {/*编辑保存*/
            dailyTrain.setUpdateTime(now);
            dailyTrainMapper.updateByPrimaryKey(dailyTrain);
        }

    }

    public PageResp<DailyTrainQueryResp> queryList(DailyTrainQueryReq req){
        DailyTrainExample dailyTrainExample = new DailyTrainExample();// 创建MyBatis的Example查询对象
        dailyTrainExample.setOrderByClause("date desc,code asc");
        DailyTrainExample.Criteria criteria = dailyTrainExample.createCriteria();    // 创建查询条件Criteria对象


        if (ObjectUtil.isNotNull(req.getDate())){// 条件判断：如果请求参数中的会员ID不为空，则添加会员ID等于条件
            criteria.andDateEqualTo(req.getDate());
        } // 执行查询，获取乘客实体列表
       if (ObjectUtil.isNotEmpty(req.getCode())){// 条件判断：如果请求参数中的会员ID不为空，则添加会员ID等于条件
            criteria.andCodeEqualTo(req.getCode());
        } // 执行查询，获取乘客实体列表

        /*在DailyTrainQueryReq req传入的参数中包含页码和页数*/
        LOG.info("查询页码：{}",req.getPage());
        LOG.info("每条页数：{}",req.getSize());
        /*分页组件*/
        PageHelper.startPage(req.getPage(), req.getSize());
        /*执行分页查询，得到的是“当前页”的数据列表，List大小 <= req.getSize()*/
        List<DailyTrain> dailyTrainList = dailyTrainMapper.selectByExample(dailyTrainExample);
        // 将实体列表转换为响应对象列表（DTO转换）
        List<DailyTrainQueryResp> List = BeanUtil.copyToList(dailyTrainList, DailyTrainQueryResp.class);
        /*获取包含“总记录数”和“当前页数据”的分页信息对象*/
        PageInfo<DailyTrain> pageInfo = new PageInfo<>(dailyTrainList);
        LOG.info("总行数：{}",pageInfo.getTotal());
        LOG.info("总页数：{}",pageInfo.getPages());
       /* 创建自定义的响应对象 PageResp*/
        PageResp<DailyTrainQueryResp> pageResp = new PageResp<>();
        /*将“总记录数”和“当前页的DTO列表”设置到自定义响应对象中*/
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(List);
        return pageResp;
    }


    public void delete(Long id){
        dailyTrainMapper.deleteByPrimaryKey(id);
    }
    /*
    *
    * 生成某日所有车次信息，包括车次车站车厢座位*/
    public void genDaily(Date date){
        List<Train> trainList = trainService.selectAll();
        if (CollUtil.isEmpty(trainList)){
            LOG.info("没有车次基础数据，任务结束");
            return;
        }
        for (Train train:
             trainList) {
            genDailyTrain(date,train);

        }

    }
    public void genDailyTrain(Date date,Train train){
        LOG.info("开始生成日期【{}】车次【{}】的信息", DateUtil.formatDate(date),train.getCode());
        //删除该车次已有的数据
        DailyTrainExample dailyTrainExample = new DailyTrainExample();// 创建MyBatis的Example查询对象
        dailyTrainExample.createCriteria()
                .andDateEqualTo(date)
                .andCodeEqualTo(train.getCode());    // 创建查询条件Criteria对象
        dailyTrainMapper.deleteByExample(dailyTrainExample);
        //生成该车次数据
        DateTime now = DateTime.now();
        DailyTrain dailyTrain = BeanUtil.copyProperties(train, DailyTrain.class);
        dailyTrain.setId(SnowUtil.getSnowflakeNextId());
        dailyTrain.setCreateTime(now);
        dailyTrain.setUpdateTime(now);
        dailyTrain.setDate(date);
        dailyTrainMapper.insert(dailyTrain);

        //生成该车次车站的数据
        dailyTrainStationService.genDaily(date,train.getCode());
        //生成该车次车厢的数据
        dailyTrainCarriageService.genDaily(date,train.getCode());
        LOG.info("生成日期【{}】车次【{}】的信息结束", DateUtil.formatDate(date),train.getCode());
    }
}