package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.domain.*;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.business.mapper.DailyTrainSeatMapper;
import com.jiawa.train.business.req.DailyTrainSeatQueryReq;
import com.jiawa.train.business.req.DailyTrainSeatSaveReq;
import com.jiawa.train.business.resp.DailyTrainSeatQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class DailyTrainSeatService {
      public static final Logger LOG = LoggerFactory.getLogger(DailyTrainSeatService.class);
    @Resource
    private DailyTrainSeatMapper dailyTrainSeatMapper;
    @Resource
    private TrainSeatService trainSeatService;
    @Resource
    private TrainStationService trainStationService;
    public void save(DailyTrainSeatSaveReq req){
        DateTime now = DateTime.now();
        // 将请求对象req的属性复制到DailyTrainSeat对象中（需要确保两个类的属性名和类型匹配）
        DailyTrainSeat dailyTrainSeat = BeanUtil.copyProperties(req, DailyTrainSeat.class);
        if (ObjectUtil.isNull(dailyTrainSeat.getId())){/*根据id判断是新增保存还是编辑保存*/
            /*新增保存*/
            dailyTrainSeat.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainSeat.setCreateTime(now);
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeatMapper.insert(dailyTrainSeat);
        }else {/*编辑保存*/
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeatMapper.updateByPrimaryKey(dailyTrainSeat);
        }

    }

    public PageResp<DailyTrainSeatQueryResp> queryList(DailyTrainSeatQueryReq req){
        DailyTrainSeatExample dailyTrainSeatExample = new DailyTrainSeatExample();// 创建MyBatis的Example查询对象
        dailyTrainSeatExample.setOrderByClause("date desc,train_code asc,carriage_seat_index asc");
        DailyTrainSeatExample.Criteria criteria = dailyTrainSeatExample.createCriteria();    // 创建查询条件Criteria对象

        if (ObjectUtil.isNotEmpty(req.getTrainCode())){// 条件判断：如果请求参数中的会员ID不为空，则添加会员ID等于条件
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        } // 执行查询，获取乘客实体列表

        /*在DailyTrainSeatQueryReq req传入的参数中包含页码和页数*/
        LOG.info("查询页码：{}",req.getPage());
        LOG.info("每条页数：{}",req.getSize());
        /*分页组件*/
        PageHelper.startPage(req.getPage(), req.getSize());
        /*执行分页查询，得到的是“当前页”的数据列表，List大小 <= req.getSize()*/
        List<DailyTrainSeat> dailyTrainSeatList = dailyTrainSeatMapper.selectByExample(dailyTrainSeatExample);
        // 将实体列表转换为响应对象列表（DTO转换）
        List<DailyTrainSeatQueryResp> List = BeanUtil.copyToList(dailyTrainSeatList, DailyTrainSeatQueryResp.class);
        /*获取包含“总记录数”和“当前页数据”的分页信息对象*/
        PageInfo<DailyTrainSeat> pageInfo = new PageInfo<>(dailyTrainSeatList);
        LOG.info("总行数：{}",pageInfo.getTotal());
        LOG.info("总页数：{}",pageInfo.getPages());
       /* 创建自定义的响应对象 PageResp*/
        PageResp<DailyTrainSeatQueryResp> pageResp = new PageResp<>();
        /*将“总记录数”和“当前页的DTO列表”设置到自定义响应对象中*/
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(List);
        return pageResp;
    }


    public void delete(Long id){
        dailyTrainSeatMapper.deleteByPrimaryKey(id);
    }
    @Transactional
    public void genDaily(Date date, String trainCode){
        LOG.info("开始生成日期【{}】车次【{}】的座位信息", DateUtil.formatDate(date),trainCode);
        //删除某日某车次的座位信息
        DailyTrainSeatExample dailyTrainSeatExample = new DailyTrainSeatExample();// 创建MyBatis的Example查询对象
        dailyTrainSeatExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode);    // 创建查询条件Criteria对象
        dailyTrainSeatMapper.deleteByExample(dailyTrainSeatExample);


        List<TrainStation> stationList = trainStationService.selectByTrainCode(trainCode);
        LOG.info("stationList大小为：{}",stationList.size());
        String sell = StrUtil.fillBefore("",'0',stationList.size() - 1);
        LOG.info("sell为：{}",sell);
        //查出某车次所有的座位信息
        List<TrainSeat> seatList = trainSeatService.selectByTrainCode(trainCode);
        if (CollUtil.isEmpty(seatList)){
            LOG.info("该车次没有座位基础数据，生成该车次座位信息结束");
            return;
        }
        for (TrainSeat trainSeat:seatList) {
            DateTime now = DateTime.now();
            DailyTrainSeat dailyTrainSeat = BeanUtil.copyProperties(trainSeat, DailyTrainSeat.class);
            dailyTrainSeat.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainSeat.setCreateTime(now);
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeat.setDate(date);
            dailyTrainSeat.setSell(sell);
            dailyTrainSeatMapper.insert(dailyTrainSeat);
            LOG.info("{}号火车的sell存储在dailyTrainSeatMapper中的值为：{}",trainSeat.getTrainCode(),dailyTrainSeat.getSell());
        }

        LOG.info("生成日期【{}】车次【{}】的座位信息结束", DateUtil.formatDate(date),trainCode);
    }

    public int countSeat(Date date, String trainCode,String seatType){
        DailyTrainSeatExample example = new DailyTrainSeatExample();
        example.createCriteria().andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode)
                .andSeatTypeEqualTo(seatType);
        long l = dailyTrainSeatMapper.countByExample(example);
        if (l == 0L){
            return -1;
        }
        return (int) l;
    }
}