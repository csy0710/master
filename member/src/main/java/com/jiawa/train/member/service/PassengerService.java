package com.jiawa.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.member.domain.Passenger;
import com.jiawa.train.member.domain.PassengerExample;
import com.jiawa.train.member.mapper.PassengerMapper;
import com.jiawa.train.member.req.PassengerQueryReq;
import com.jiawa.train.member.req.PassengerSaveReq;
import com.jiawa.train.member.resp.PassengerQueryResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PassengerService {
    @Resource
    private PassengerMapper passengerMapper;
    public void save(PassengerSaveReq req){
        DateTime now = DateTime.now();
        // 将请求对象req的属性复制到Passenger对象中（需要确保两个类的属性名和类型匹配）
        Passenger passenger = BeanUtil.copyProperties(req, Passenger.class);
        passenger.setMemberId(LoginMemberContext.getId());//拦截器获取headers数据，得到用户id
        passenger.setId(SnowUtil.getSnowflakeNextId());
        passenger.setCreateTime(now);
        passenger.setUpdateTime(now);
        passengerMapper.insert(passenger);
    }

    public List<PassengerQueryResp> queryList(PassengerQueryReq req){
        PassengerExample passengerExample = new PassengerExample();// 创建MyBatis的Example查询对象
        PassengerExample.Criteria criteria = passengerExample.createCriteria();    // 创建查询条件Criteria对象
        if (ObjectUtil.isNotNull(req.getMemberId())){// 条件判断：如果请求参数中的会员ID不为空，则添加会员ID等于条件
            criteria.andMemberIdEqualTo(req.getMemberId());
        } // 执行查询，获取乘客实体列表
        PageHelper.startPage(req.getPage(), req.getSize());
        List<Passenger> passengerList = passengerMapper.selectByExample(passengerExample);
        // 将实体列表转换为响应对象列表（DTO转换）
        List<PassengerQueryResp> List = BeanUtil.copyToList(passengerList, PassengerQueryResp.class);
        return List;
    }


}
