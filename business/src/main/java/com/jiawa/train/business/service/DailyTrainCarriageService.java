package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.enums.SeatColEnum;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.business.domain.DailyTrainCarriage;
import com.jiawa.train.business.domain.DailyTrainCarriageExample;
import com.jiawa.train.business.mapper.DailyTrainCarriageMapper;
import com.jiawa.train.business.req.DailyTrainCarriageQueryReq;
import com.jiawa.train.business.req.DailyTrainCarriageSaveReq;
import com.jiawa.train.business.resp.DailyTrainCarriageQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DailyTrainCarriageService {
      public static final Logger LOG = LoggerFactory.getLogger(DailyTrainCarriageService.class);
    @Resource
    private DailyTrainCarriageMapper dailyTrainCarriageMapper;
    public void save(DailyTrainCarriageSaveReq req){
        DateTime now = DateTime.now();
        // 自动计算出列数和总座位数
        List<SeatColEnum> seatColEnums = SeatColEnum.getColsByType(req.getSeatType());
        req.setColCount(seatColEnums.size());
        req.setSeatCount(req.getColCount() * req.getRowCount());
        // 将请求对象req的属性复制到DailyTrainCarriage对象中（需要确保两个类的属性名和类型匹配）
        DailyTrainCarriage dailyTrainCarriage = BeanUtil.copyProperties(req, DailyTrainCarriage.class);
        if (ObjectUtil.isNull(dailyTrainCarriage.getId())){/*根据id判断是新增保存还是编辑保存*/
            /*新增保存*/
            dailyTrainCarriage.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainCarriage.setCreateTime(now);
            dailyTrainCarriage.setUpdateTime(now);
            dailyTrainCarriageMapper.insert(dailyTrainCarriage);
        }else {/*编辑保存*/
            dailyTrainCarriage.setUpdateTime(now);
            dailyTrainCarriageMapper.updateByPrimaryKey(dailyTrainCarriage);
        }

    }

    public PageResp<DailyTrainCarriageQueryResp> queryList(DailyTrainCarriageQueryReq req){
        DailyTrainCarriageExample dailyTrainCarriageExample = new DailyTrainCarriageExample();// 创建MyBatis的Example查询对象
        dailyTrainCarriageExample.setOrderByClause("date desc,train_code asc,`index` asc");
        DailyTrainCarriageExample.Criteria criteria = dailyTrainCarriageExample.createCriteria();    // 创建查询条件Criteria对象
        if (ObjectUtil.isNotNull(req.getDate())){// 条件判断：如果请求参数中的会员ID不为空，则添加会员ID等于条件
            criteria.andDateEqualTo(req.getDate());
        } // 执行查询，获取乘客实体列表
        if (ObjectUtil.isNotEmpty(req.getTrainCode())){// 条件判断：如果请求参数中的会员ID不为空，则添加会员ID等于条件
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        } // 执行查询，获取乘客实体列表
        /*在DailyTrainCarriageQueryReq req传入的参数中包含页码和页数*/
        LOG.info("查询页码：{}",req.getPage());
        LOG.info("每条页数：{}",req.getSize());
        /*分页组件*/
        PageHelper.startPage(req.getPage(), req.getSize());
        /*执行分页查询，得到的是“当前页”的数据列表，List大小 <= req.getSize()*/
        List<DailyTrainCarriage> dailyTrainCarriageList = dailyTrainCarriageMapper.selectByExample(dailyTrainCarriageExample);
        // 将实体列表转换为响应对象列表（DTO转换）
        List<DailyTrainCarriageQueryResp> List = BeanUtil.copyToList(dailyTrainCarriageList, DailyTrainCarriageQueryResp.class);
        /*获取包含“总记录数”和“当前页数据”的分页信息对象*/
        PageInfo<DailyTrainCarriage> pageInfo = new PageInfo<>(dailyTrainCarriageList);
        LOG.info("总行数：{}",pageInfo.getTotal());
        LOG.info("总页数：{}",pageInfo.getPages());
       /* 创建自定义的响应对象 PageResp*/
        PageResp<DailyTrainCarriageQueryResp> pageResp = new PageResp<>();
        /*将“总记录数”和“当前页的DTO列表”设置到自定义响应对象中*/
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(List);
        return pageResp;
    }


    public void delete(Long id){
        dailyTrainCarriageMapper.deleteByPrimaryKey(id);
    }

}