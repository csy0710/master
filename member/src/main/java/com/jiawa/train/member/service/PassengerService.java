package com.jiawa.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.member.domain.Passenger;
import com.jiawa.train.member.domain.PassengerExample;
import com.jiawa.train.member.mapper.PassengerMapper;
import com.jiawa.train.member.req.PassengerQueryReq;
import com.jiawa.train.member.req.PassengerSaveReq;
import com.jiawa.train.member.resp.PassengerQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PassengerService {
      public static final Logger LOG = LoggerFactory.getLogger(PassengerService.class);
    @Resource
    private PassengerMapper passengerMapper;
    public void save(PassengerSaveReq req){
        DateTime now = DateTime.now();
        // 将请求对象req的属性复制到Passenger对象中（需要确保两个类的属性名和类型匹配）
        Passenger passenger = BeanUtil.copyProperties(req, Passenger.class);
        if (ObjectUtil.isNull(passenger.getId())){/*根据id判断是新增保存还是编辑保存*/
            /*新增保存*/
            passenger.setMemberId(LoginMemberContext.getId());//拦截器获取headers数据，得到用户id
            passenger.setId(SnowUtil.getSnowflakeNextId());
            passenger.setCreateTime(now);
            passenger.setUpdateTime(now);
            passengerMapper.insert(passenger);
        }else {/*编辑保存*/
            passenger.setUpdateTime(now);
            passengerMapper.updateByPrimaryKey(passenger);
        }

    }

    public PageResp<PassengerQueryResp> queryList(PassengerQueryReq req){
        PassengerExample passengerExample = new PassengerExample();// 创建MyBatis的Example查询对象
        passengerExample.setOrderByClause("id desc");
        PassengerExample.Criteria criteria = passengerExample.createCriteria();    // 创建查询条件Criteria对象
        if (ObjectUtil.isNotNull(req.getMemberId())){// 条件判断：如果请求参数中的会员ID不为空，则添加会员ID等于条件
            criteria.andMemberIdEqualTo(req.getMemberId());
        } // 执行查询，获取乘客实体列表
        /*在PassengerQueryReq req传入的参数中包含页码和页数*/
        LOG.info("查询页码：{}",req.getPage());
        LOG.info("每条页数：{}",req.getSize());
        /*分页组件*/
        PageHelper.startPage(req.getPage(), req.getSize());
        /*执行分页查询，得到的是“当前页”的数据列表，List大小 <= req.getSize()*/
        List<Passenger> passengerList = passengerMapper.selectByExample(passengerExample);
        // 将实体列表转换为响应对象列表（DTO转换）
        List<PassengerQueryResp> List = BeanUtil.copyToList(passengerList, PassengerQueryResp.class);
        /*获取包含“总记录数”和“当前页数据”的分页信息对象*/
        PageInfo<Passenger> pageInfo = new PageInfo<>(passengerList);
        LOG.info("总行数：{}",pageInfo.getTotal());
        LOG.info("总页数：{}",pageInfo.getPages());
       /* 创建自定义的响应对象 PageResp*/
        PageResp<PassengerQueryResp> pageResp = new PageResp<>();
        /*将“总记录数”和“当前页的DTO列表”设置到自定义响应对象中*/
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(List);
        return pageResp;
    }


    public void delete(Long id){
        passengerMapper.deleteByPrimaryKey(id);
    }

    public List<PassengerQueryResp> queryMine(){
        PassengerExample passengerExample = new PassengerExample();
        passengerExample.setOrderByClause("name asc");
        PassengerExample.Criteria criteria = passengerExample.createCriteria();
        criteria.andMemberIdEqualTo(LoginMemberContext.getId());
        List<Passenger> list = passengerMapper.selectByExample(passengerExample);
        return BeanUtil.copyToList(list,PassengerQueryResp.class);
    }

}