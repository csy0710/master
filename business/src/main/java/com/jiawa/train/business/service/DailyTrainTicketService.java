package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.domain.DailyTrain;
import com.jiawa.train.business.domain.DailyTrainTicket;
import com.jiawa.train.business.domain.DailyTrainTicketExample;
import com.jiawa.train.business.domain.TrainStation;
import com.jiawa.train.business.enums.SeatTypeEnum;
import com.jiawa.train.business.enums.TrainTypeEnum;
import com.jiawa.train.business.mapper.DailyTrainTicketMapper;
import com.jiawa.train.business.req.DailyTrainTicketQueryReq;
import com.jiawa.train.business.req.DailyTrainTicketSaveReq;
import com.jiawa.train.business.resp.DailyTrainTicketQueryResp;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@Service
public class DailyTrainTicketService {
      public static final Logger LOG = LoggerFactory.getLogger(DailyTrainTicketService.class);
    @Resource
    private DailyTrainTicketMapper dailyTrainTicketMapper;
    @Resource
    private TrainStationService trainStationService;
    @Resource
    private DailyTrainSeatService dailyTrainSeatService;
    public void save(DailyTrainTicketSaveReq req){
        DateTime now = DateTime.now();
        // 将请求对象req的属性复制到DailyTrainTicket对象中（需要确保两个类的属性名和类型匹配）
        DailyTrainTicket dailyTrainTicket = BeanUtil.copyProperties(req, DailyTrainTicket.class);
        if (ObjectUtil.isNull(dailyTrainTicket.getId())){/*根据id判断是新增保存还是编辑保存*/
            /*新增保存*/
            dailyTrainTicket.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainTicket.setCreateTime(now);
            dailyTrainTicket.setUpdateTime(now);
            dailyTrainTicketMapper.insert(dailyTrainTicket);
        }else {/*编辑保存*/
            dailyTrainTicket.setUpdateTime(now);
            dailyTrainTicketMapper.updateByPrimaryKey(dailyTrainTicket);
        }

    }

    public PageResp<DailyTrainTicketQueryResp> queryList(DailyTrainTicketQueryReq req){
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();// 创建MyBatis的Example查询对象
        dailyTrainTicketExample.setOrderByClause("id desc");
        DailyTrainTicketExample.Criteria criteria = dailyTrainTicketExample.createCriteria();    // 创建查询条件Criteria对象
        if (ObjUtil.isNotNull(req.getDate())) {
            criteria.andDateEqualTo(req.getDate());
        }
        if (ObjUtil.isNotEmpty(req.getTrainCode())) {
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        }
        if (ObjUtil.isNotEmpty(req.getStart())) {
            criteria.andStartEqualTo(req.getStart());
        }
        if (ObjUtil.isNotEmpty(req.getEnd())) {
            criteria.andEndEqualTo(req.getEnd());
        }

        /*在DailyTrainTicketQueryReq req传入的参数中包含页码和页数*/
        LOG.info("查询页码：{}",req.getPage());
        LOG.info("每条页数：{}",req.getSize());
        /*分页组件*/
        PageHelper.startPage(req.getPage(), req.getSize());
        /*执行分页查询，得到的是“当前页”的数据列表，List大小 <= req.getSize()*/
        List<DailyTrainTicket> dailyTrainTicketList = dailyTrainTicketMapper.selectByExample(dailyTrainTicketExample);
        // 将实体列表转换为响应对象列表（DTO转换）
        List<DailyTrainTicketQueryResp> List = BeanUtil.copyToList(dailyTrainTicketList, DailyTrainTicketQueryResp.class);
        /*获取包含“总记录数”和“当前页数据”的分页信息对象*/
        PageInfo<DailyTrainTicket> pageInfo = new PageInfo<>(dailyTrainTicketList);
        LOG.info("总行数：{}",pageInfo.getTotal());
        LOG.info("总页数：{}",pageInfo.getPages());
       /* 创建自定义的响应对象 PageResp*/
        PageResp<DailyTrainTicketQueryResp> pageResp = new PageResp<>();
        /*将“总记录数”和“当前页的DTO列表”设置到自定义响应对象中*/
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(List);
        return pageResp;
    }


    public void delete(Long id){
        dailyTrainTicketMapper.deleteByPrimaryKey(id);
    }
    @Transactional   //批量操作考虑增加事务 如果跑一半失败了，是全部回滚还是跑多少算多少 @Transactional保证要么全部成功，要么全部失败
    public void genDaily(DailyTrain dailyTrain, Date date, String trainCode){
        LOG.info("开始生成日期【{}】车次【{}】的余票信息", DateUtil.formatDate(date),trainCode);
        //删除某日某车次的车站信息
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();// 创建MyBatis的Example查询对象
        dailyTrainTicketExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode);    // 创建查询条件Criteria对象
        dailyTrainTicketMapper.deleteByExample(dailyTrainTicketExample);

        //查询途径的车站信息
        List<TrainStation> stationList = trainStationService.selectByTrainCode(trainCode);
        if (CollUtil.isEmpty(stationList)){
            LOG.info("该车次没有车站基础数据，生成该车次余票信息结束");
            return;
        }
        DateTime now = DateTime.now();
        //使用单向嵌套循环
        for (int i = 0; i < stationList.size(); i++) {
            //得到出发站
            TrainStation trainStationStart = stationList.get(i);
            //得到出发站的距离
            BigDecimal sunKm = BigDecimal.ZERO;
            for (int j = (i+1); j <stationList.size(); j++) {
                //得到下车站
                TrainStation trainStationEnd = stationList.get(j);
                //计算里程之和
                sunKm = sunKm.add(trainStationEnd.getKm());
                DailyTrainTicket dailyTrainTicket = new DailyTrainTicket();

                dailyTrainTicket.setId(SnowUtil.getSnowflakeNextId());
                dailyTrainTicket.setDate(date);
                dailyTrainTicket.setTrainCode(trainCode);
                dailyTrainTicket.setStart(trainStationStart.getName());
                dailyTrainTicket.setStartPinyin(trainStationStart.getNamePinyin());
                dailyTrainTicket.setStartTime(trainStationStart.getOutTime());
                dailyTrainTicket.setStartIndex(trainStationStart.getIndex());
                dailyTrainTicket.setEnd(trainStationEnd.getName());
                dailyTrainTicket.setEndPinyin(trainStationEnd.getNamePinyin());
                dailyTrainTicket.setEndTime(trainStationEnd.getInTime());
                dailyTrainTicket.setEndIndex(trainStationEnd.getIndex());
                int ydz = dailyTrainSeatService.countSeat(date, trainCode, SeatTypeEnum.YDZ.getCode());
                int edz = dailyTrainSeatService.countSeat(date, trainCode, SeatTypeEnum.EDZ.getCode());
                int rw = dailyTrainSeatService.countSeat(date, trainCode, SeatTypeEnum.RW.getCode());
                int yw = dailyTrainSeatService.countSeat(date, trainCode, SeatTypeEnum.YW.getCode());
                String trainType = dailyTrain.getType();
//                计算票价系数TrainTypeEnum.priceRate
                BigDecimal priceRate = EnumUtil.getFieldBy(TrainTypeEnum::getPriceRate, TrainTypeEnum::getCode, trainType);
                //                票价 = 里程之和 * 座位单价 *车次类型系数
                BigDecimal ydzPrice = sunKm.multiply(SeatTypeEnum.YDZ.getPrice()).multiply(priceRate).setScale(2, RoundingMode.HALF_UP);//保留两位小数,四舍五入
                BigDecimal edzPrice = sunKm.multiply(SeatTypeEnum.EDZ.getPrice()).multiply(priceRate).setScale(2, RoundingMode.HALF_UP);//保留两位小数,四舍五入
                BigDecimal rwPrice = sunKm.multiply(SeatTypeEnum.RW.getPrice()).multiply(priceRate).setScale(2, RoundingMode.HALF_UP);//保留两位小数,四舍五入
                BigDecimal ywPrice = sunKm.multiply(SeatTypeEnum.YW.getPrice()).multiply(priceRate).setScale(2, RoundingMode.HALF_UP);//保留两位小数,四舍五入
                dailyTrainTicket.setYdz(ydz);
                dailyTrainTicket.setYdzPrice(ydzPrice);
                dailyTrainTicket.setEdz(edz);
                dailyTrainTicket.setEdzPrice(edzPrice);
                dailyTrainTicket.setRw(rw);
                dailyTrainTicket.setRwPrice(rwPrice);
                dailyTrainTicket.setYw(yw);
                dailyTrainTicket.setYwPrice(ywPrice);
                dailyTrainTicket.setCreateTime(now);
                dailyTrainTicket.setUpdateTime(now);
                dailyTrainTicketMapper.insert(dailyTrainTicket);

            }
        }
        LOG.info("生成日期【{}】车次【{}】的车站信息结束", DateUtil.formatDate(date),trainCode);
    }

}