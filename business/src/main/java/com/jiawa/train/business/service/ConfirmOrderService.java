package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.domain.*;
import com.jiawa.train.business.enums.ConfirmOrderStatusEnum;
import com.jiawa.train.business.enums.SeatColEnum;
import com.jiawa.train.business.enums.SeatTypeEnum;
import com.jiawa.train.business.mapper.ConfirmOrderMapper;
import com.jiawa.train.business.req.ConfirmOrderDoReq;
import com.jiawa.train.business.req.ConfirmOrderQueryReq;
import com.jiawa.train.business.req.ConfirmOrderTicketReq;
import com.jiawa.train.business.resp.ConfirmOrderQueryResp;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.exception.BusinessException;
import com.jiawa.train.common.exception.BusinessExceptionEnum;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ConfirmOrderService {
      public static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderService.class);
    @Resource
    private ConfirmOrderMapper confirmOrderMapper;
    @Resource
    private DailyTrainTicketService dailyTrainTicketService;
    @Resource
    private DailyTrainCarriageService dailyTrainCarriageService;

    @Resource
    private DailyTrainSeatService dailyTrainSeatService;
    public void save(ConfirmOrderDoReq req){
        DateTime now = DateTime.now();
        // 将请求对象req的属性复制到ConfirmOrder对象中（需要确保两个类的属性名和类型匹配）
        ConfirmOrder confirmOrder = BeanUtil.copyProperties(req, ConfirmOrder.class);
        if (ObjectUtil.isNull(confirmOrder.getId())){/*根据id判断是新增保存还是编辑保存*/
            /*新增保存*/
            confirmOrder.setId(SnowUtil.getSnowflakeNextId());
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.insert(confirmOrder);
        }else {/*编辑保存*/
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.updateByPrimaryKey(confirmOrder);
        }

    }

    public PageResp<ConfirmOrderQueryResp> queryList(ConfirmOrderQueryReq req){
        ConfirmOrderExample confirmOrderExample = new ConfirmOrderExample();// 创建MyBatis的Example查询对象
        confirmOrderExample.setOrderByClause("id desc");
        ConfirmOrderExample.Criteria criteria = confirmOrderExample.createCriteria();    // 创建查询条件Criteria对象

        /*在ConfirmOrderQueryReq req传入的参数中包含页码和页数*/
        LOG.info("查询页码：{}",req.getPage());
        LOG.info("每条页数：{}",req.getSize());
        /*分页组件*/
        PageHelper.startPage(req.getPage(), req.getSize());
        /*执行分页查询，得到的是“当前页”的数据列表，List大小 <= req.getSize()*/
        List<ConfirmOrder> confirmOrderList = confirmOrderMapper.selectByExample(confirmOrderExample);
        // 将实体列表转换为响应对象列表（DTO转换）
        List<ConfirmOrderQueryResp> List = BeanUtil.copyToList(confirmOrderList, ConfirmOrderQueryResp.class);
        /*获取包含“总记录数”和“当前页数据”的分页信息对象*/
        PageInfo<ConfirmOrder> pageInfo = new PageInfo<>(confirmOrderList);
        LOG.info("总行数：{}",pageInfo.getTotal());
        LOG.info("总页数：{}",pageInfo.getPages());
       /* 创建自定义的响应对象 PageResp*/
        PageResp<ConfirmOrderQueryResp> pageResp = new PageResp<>();
        /*将“总记录数”和“当前页的DTO列表”设置到自定义响应对象中*/
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(List);
        return pageResp;
    }


    public void delete(Long id){
        confirmOrderMapper.deleteByPrimaryKey(id);
    }


    public void doConfirm(ConfirmOrderDoReq req){

        Date date = req.getDate();
        String trainCode = req.getTrainCode();
        String start = req.getStart();
        String end = req.getEnd();
        List<ConfirmOrderTicketReq> tickets = req.getTickets();
        //保存确认订单表，将订单状态设置为初始
        DateTime now = DateTime.now();
        ConfirmOrder confirmOrder = new ConfirmOrder();
        confirmOrder.setId(SnowUtil.getSnowflakeNextId());
        confirmOrder.setCreateTime(now);
        confirmOrder.setUpdateTime(now);
        confirmOrder.setMemberId(LoginMemberContext.getId());
        confirmOrder.setDate(date);
        confirmOrder.setTrainCode(trainCode);
        confirmOrder.setStart(start);
        confirmOrder.setEnd(end);
        confirmOrder.setDailyTrainTicketId(req.getDailyTrainTicketId());
        confirmOrder.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
        confirmOrder.setTickets(JSON.toJSONString(tickets));
        confirmOrderMapper.insert(confirmOrder);
        //查出余票记录，需要得到真实的库存
        DailyTrainTicket dailyTrainTicket = dailyTrainTicketService.selectByUnique(date, trainCode, start, end);
        LOG.info("查出余票记录：{}",dailyTrainTicket);
        //预扣减余票数量，并判断余票是否足够
        reduceTickets(req, dailyTrainTicket);
        //计算相对第一个座位的偏移值
//        判断是否有选座 获取到第一张票的信息中是否有座位信息
        ConfirmOrderTicketReq ticketReq0 = tickets.get(0);
        if(StrUtil.isNotBlank(ticketReq0.getSeat())){
        LOG.info("本次购票有选座");
            //        若有选座查出本次选座的作为类型有哪些列
            List<SeatColEnum> colEnumList = SeatColEnum.getColsByType(ticketReq0.getSeatTypeCode());
            LOG.info("本次选座包含的列：{}",colEnumList);
            //在后端组成和前端一样两排的选座列表，用于参照的座位列表
            List<String> referSeatList = new ArrayList<>();
            for (int i=1; i <= 2;i++){
                for (SeatColEnum seatColEnum: colEnumList) {
                    referSeatList.add(seatColEnum.getCode() + i);
                }
            }
            LOG.info("用于做参照的两排座位：{}",referSeatList);
//            计算偏移值
//计算索引
                    List<Integer> aboluteOffsetList = new ArrayList<>();
                    List<Integer> offsetList = new ArrayList<>();
            for (ConfirmOrderTicketReq ticketReq:tickets) {
                int index = referSeatList.indexOf(ticketReq.getSeat());
                aboluteOffsetList.add(index);
            }
            LOG.info("获取所有座位的索引值：{}",aboluteOffsetList);
            for (Integer index:aboluteOffsetList) {
                int offset = index - aboluteOffsetList.get(0);
                offsetList.add(offset);
            }
            LOG.info("计算得到所有座位的相对偏移值：{}",offsetList);
            getSeat(date,
                    trainCode,
                    ticketReq0.getSeatTypeCode(),
                    ticketReq0.getSeat().split("")[0],
                    offsetList,
                    dailyTrainTicket.getStartIndex(),
                    dailyTrainTicket.getEndIndex()
            );
        }else {

            LOG.info("本次购票无选座");
            for (ConfirmOrderTicketReq ticketReq:tickets) {
                getSeat(date,
                        trainCode,
                        ticketReq.getSeatTypeCode(),
                        null,
                        null,
                        dailyTrainTicket.getStartIndex(),
                        dailyTrainTicket.getEndIndex()
                );
            }
        }


    }
    private void getSeat(Date date,String trainCode, String seatType,String column,List<Integer> offsetList,Integer startIndex, Integer endIndex){

        List<DailyTrainCarriage> carriageList = dailyTrainCarriageService.selectBySeatType(date, trainCode, seatType);

        LOG.info("共查出{}个符合条件的车厢",carriageList.size());
//        一个车厢一个车厢获取座位数据

        for (DailyTrainCarriage dailyTrainCarriage: carriageList){
            LOG.info("开始从车厢{}选座",dailyTrainCarriage.getIndex());
            List<DailyTrainSeat> seatList = dailyTrainSeatService.selectByCarriage(date, trainCode, dailyTrainCarriage.getIndex());
            LOG.info("车厢{}的座位数：{}",dailyTrainCarriage.getIndex(),seatList.size());

            for (int i = 0; i < seatList.size(); i++) {
                DailyTrainSeat dailyTrainSeat = seatList.get(i);
                Integer seatIndex = dailyTrainSeat.getCarriageSeatIndex();
                String col = dailyTrainSeat.getCol();


                // 判断column，有值的话要比对列号
                if (StrUtil.isBlank(column)) {
                    LOG.info("无选座");
                } else {
                    if (!column.equals(col)) {
                        LOG.info("座位{}列值不对，继续判断下一个座位，当前列值：{}，目标列值：{}", seatIndex, col, column);
                        continue;
                    }
                }


                boolean isChoose = calSell(dailyTrainSeat, startIndex, endIndex);
                if (isChoose) {
                    LOG.info("选中座位");

//                    getSeatList.add(dailyTrainSeat);
                } else {
                    continue;
                }


                // 根据offset选剩下的座位

                boolean isGetAllOffsetSeat = true;

                if (CollUtil.isNotEmpty(offsetList)) {
                    LOG.info("有偏移值：{}，校验偏移的座位是否可选", offsetList);
                    // 从索引1开始，索引0就是当前已选中的票
                    for (int j = 1; j < offsetList.size(); j++) {
                        Integer offset = offsetList.get(j);
                        // 座位在库的索引是从1开始
//                         int nextIndex = seatIndex + offset - 1;
                        int nextIndex = i + offset;

                        // 有选座时，一定是在同一个车箱
                        if (nextIndex >= seatList.size()) {
                            LOG.info("座位{}不可选，偏移后的索引超出了这个车箱的座位数", nextIndex);
                            isGetAllOffsetSeat = false;
                            break;
                        }

                        DailyTrainSeat nextDailyTrainSeat = seatList.get(nextIndex);
                        boolean isChooseNext = calSell(nextDailyTrainSeat, startIndex, endIndex);
                        if (isChooseNext) {
                            LOG.info("座位{}被选中", nextDailyTrainSeat.getCarriageSeatIndex());
//                            getSeatList.add(nextDailyTrainSeat);
                        } else {
                            LOG.info("座位{}不可选", nextDailyTrainSeat.getCarriageSeatIndex());
                            isGetAllOffsetSeat = false;
                            break;
                        }
                    }
                }

                if (!isGetAllOffsetSeat) {
                    continue;
                }
                // 保存选好的座位

                return;


            }
        }


    }
    /*计算座位在区间内是否可卖*/
    private boolean calSell(DailyTrainSeat dailyTrainSeat, Integer startIndex, Integer endIndex) {
        //        例如10001
        String sell = dailyTrainSeat.getSell();
        //售卖区间为000
        String sellPart = sell.substring(startIndex, endIndex);
        if (Integer.parseInt(sellPart) > 0) {
            LOG.info("座位{}在本次车站区间{}~{}已售过票，不可选中该座位", dailyTrainSeat.getCarriageSeatIndex(), startIndex, endIndex);
            return false;
        } else {
            LOG.info("座位{}在本次车站区间{}~{}未售过票，可选中该座位", dailyTrainSeat.getCarriageSeatIndex(), startIndex, endIndex);
            //  111,   111
            String curSell = sellPart.replace('0', '1');
            // 0111,  0111
            curSell = StrUtil.fillBefore(curSell, '0', endIndex);
            // 01110, 01110
            curSell = StrUtil.fillAfter(curSell, '0', sell.length());

            // 当前区间售票信息curSell 01110与库里的已售信息sell 00001按位与，即可得到该座位卖出此票后的售票详情
            // 15(01111), 14(01110 = 01110|00000)
            int newSellInt = NumberUtil.binaryToInt(curSell) | NumberUtil.binaryToInt(sell);
            //  1111,  1110
            String newSell = NumberUtil.getBinaryStr(newSellInt);
            // 01111, 01110
            newSell = StrUtil.fillBefore(newSell, '0', sell.length());
            LOG.info("座位{}被选中，原售票信息：{}，车站区间：{}~{}，即：{}，最终售票信息：{}"
                    , dailyTrainSeat.getCarriageSeatIndex(), sell, startIndex, endIndex, curSell, newSell);
            dailyTrainSeat.setSell(newSell);
            return true;
        }
    }
    private static void reduceTickets(ConfirmOrderDoReq req, DailyTrainTicket dailyTrainTicket) {
        for (ConfirmOrderTicketReq ticketReq : req.getTickets()) {
            String seatTypeCode = ticketReq.getSeatTypeCode();
            SeatTypeEnum seatTypeEnum = EnumUtil.getBy(SeatTypeEnum::getCode, seatTypeCode);
            switch (seatTypeEnum){
                case YDZ ->{
                    int countLeft = dailyTrainTicket.getYdz() - 1;
                    if (countLeft<0){
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setYdz(countLeft);
                }
                case EDZ ->{
                    int countLeft = dailyTrainTicket.getEdz() - 1;
                    if (countLeft<0){
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setEdz(countLeft);
                }case YW ->{
                    int countLeft = dailyTrainTicket.getYw() - 1;
                    if (countLeft<0){
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setYw(countLeft);
                }case RW ->{
                    int countLeft = dailyTrainTicket.getRw() - 1;
                    if (countLeft<0){
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setRw(countLeft);
                }
            }
        }
    }
}