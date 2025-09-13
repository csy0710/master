package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.domain.TrainCarriage;
import com.jiawa.train.business.domain.TrainSeat;
import com.jiawa.train.business.domain.TrainSeatExample;
import com.jiawa.train.business.enums.SeatColEnum;
import com.jiawa.train.business.mapper.TrainSeatMapper;
import com.jiawa.train.business.req.TrainSeatQueryReq;
import com.jiawa.train.business.req.TrainSeatSaveReq;
import com.jiawa.train.business.resp.TrainSeatQueryResp;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainSeatService {
      public static final Logger LOG = LoggerFactory.getLogger(TrainSeatService.class);
    @Resource
    private TrainSeatMapper trainSeatMapper;
    @Resource
    private TrainCarriageService trainCarriageService;
    public void save(TrainSeatSaveReq req){
        DateTime now = DateTime.now();
        // 将请求对象req的属性复制到TrainSeat对象中（需要确保两个类的属性名和类型匹配）
        TrainSeat trainSeat = BeanUtil.copyProperties(req, TrainSeat.class);
        if (ObjectUtil.isNull(trainSeat.getId())){/*根据id判断是新增保存还是编辑保存*/
            /*新增保存*/
            trainSeat.setId(SnowUtil.getSnowflakeNextId());
            trainSeat.setCreateTime(now);
            trainSeat.setUpdateTime(now);
            trainSeatMapper.insert(trainSeat);
        }else {/*编辑保存*/
            trainSeat.setUpdateTime(now);
            trainSeatMapper.updateByPrimaryKey(trainSeat);
        }

    }

    public PageResp<TrainSeatQueryResp> queryList(TrainSeatQueryReq req){
        TrainSeatExample trainSeatExample = new TrainSeatExample();// 创建MyBatis的Example查询对象
        trainSeatExample.setOrderByClause("train_code asc,carriage_index asc,carriage_seat_index asc");
        TrainSeatExample.Criteria criteria = trainSeatExample.createCriteria();    // 创建查询条件Criteria对象

        if (ObjectUtil.isNotEmpty(req.getTrainCode())){// 条件判断：如果请求参数中的会员ID不为空，则添加会员ID等于条件
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        } // 执行查询，获取乘客实体列表
        /*在TrainSeatQueryReq req传入的参数中包含页码和页数*/
        LOG.info("查询页码：{}",req.getPage());
        LOG.info("每条页数：{}",req.getSize());
        /*分页组件*/
        PageHelper.startPage(req.getPage(), req.getSize());
        /*执行分页查询，得到的是“当前页”的数据列表，List大小 <= req.getSize()*/
        List<TrainSeat> trainSeatList = trainSeatMapper.selectByExample(trainSeatExample);
        // 将实体列表转换为响应对象列表（DTO转换）
        List<TrainSeatQueryResp> List = BeanUtil.copyToList(trainSeatList, TrainSeatQueryResp.class);
        /*获取包含“总记录数”和“当前页数据”的分页信息对象*/
        PageInfo<TrainSeat> pageInfo = new PageInfo<>(trainSeatList);
        LOG.info("总行数：{}",pageInfo.getTotal());
        LOG.info("总页数：{}",pageInfo.getPages());
       /* 创建自定义的响应对象 PageResp*/
        PageResp<TrainSeatQueryResp> pageResp = new PageResp<>();
        /*将“总记录数”和“当前页的DTO列表”设置到自定义响应对象中*/
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(List);
        return pageResp;
    }


    public void delete(Long id){
        trainSeatMapper.deleteByPrimaryKey(id);
    }

    @Transactional     //增加事务处理，要么生成成功，要么生成失败
    public void genTrainSeat(String trainCode){
        DateTime now = DateTime.now();
        //清空当前车次下所有的座位记录
        TrainSeatExample trainSeatExample = new TrainSeatExample();// 创建MyBatis的Example查询对象
        TrainSeatExample.Criteria criteria = trainSeatExample.createCriteria();    // 创建查询条件Criteria对象
        criteria.andTrainCodeEqualTo(trainCode);
        trainSeatMapper.deleteByExample(trainSeatExample);//删除所有车次下的数据
        //查找当前车次下的所有的车厢
        List<TrainCarriage> carriageList = trainCarriageService.selectByTrainCode(trainCode);
        LOG.info("查找当前车次下的车厢数：{}",carriageList.size());
        //循环生成每个车厢的座位
        for (TrainCarriage trainCarriage:carriageList){
            //拿到车厢数据，行数，座位类型（得到列数）
            Integer rowCount = trainCarriage.getRowCount();
            String seatType = trainCarriage.getSeatType();
            int seatIndex = 1;
            //根据车厢的座位类型筛选出所有的列，比如车厢是一等座那么筛选出来columnList是（ABCD）
            List<SeatColEnum> colEnumList = SeatColEnum.getColsByType(seatType);
            LOG.info("根据车厢的座位类型,筛选出所有的列：{}",colEnumList);
            //循环行数
            for (int row = 1; row <= rowCount; row++) {
                //循环列数
                for (SeatColEnum seatColEnum: colEnumList)
                {
                    //构造座位数据并保存数据库
                    TrainSeat trainSeat = new TrainSeat();
                    trainSeat.setId(SnowUtil.getSnowflakeNextId());
                    trainSeat.setTrainCode(trainCode);
                    trainSeat.setCarriageIndex(trainCarriage.getIndex());
                    trainSeat.setRow(StrUtil.fillBefore(String.valueOf(row), '0', 2));//使用hutool工具填充数字，是两位不填充，不够两位填充0
                    trainSeat.setCol(seatColEnum.getCode());
                    trainSeat.setSeatType(seatType);
                    trainSeat.setCarriageSeatIndex(seatIndex++);
                    trainSeat.setCreateTime(now);
                    trainSeat.setUpdateTime(now);
                    trainSeatMapper.insert(trainSeat);

                }


            }
        }

    }

    public List<TrainSeat> selectByTrainCode(String trainCode){
        //清空当前车次下所有的座位记录
        TrainSeatExample trainSeatExample = new TrainSeatExample();// 创建MyBatis的Example查询对象
        trainSeatExample.setOrderByClause("`id` asc");
        TrainSeatExample.Criteria criteria = trainSeatExample.createCriteria();    // 创建查询条件Criteria对象
        criteria.andTrainCodeEqualTo(trainCode);
        return trainSeatMapper.selectByExample(trainSeatExample);
    };

}