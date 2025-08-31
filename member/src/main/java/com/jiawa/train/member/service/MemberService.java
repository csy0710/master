package com.jiawa.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.jiawa.train.common.exception.BusinessException;
import com.jiawa.train.common.exception.BusinessExceptionEnum;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.member.domain.Member;
import com.jiawa.train.member.domain.MemberExample;
import com.jiawa.train.member.mapper.MemberMapper;
import com.jiawa.train.member.req.MemberLoginReq;
import com.jiawa.train.member.req.MemberRegisterReq;
import com.jiawa.train.member.req.MemberSendCodeReq;
import com.jiawa.train.member.resp.MemberLoginResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {
    public static final Logger LOG= LoggerFactory.getLogger(MemberService.class);//日志容器
    @Resource
    private MemberMapper memberMapper;
    public int count(){
        return Math.toIntExact(memberMapper.countByExample(null));
    }

//注册
    public long register(MemberRegisterReq req) {
        String mobile=req.getMobile();
        Member memberDB = selectByMobile(mobile);//memberDB是从数据库查出来的
        if (ObjectUtil.isNotNull(memberDB)){//判断数据库查出来的是不是空，如果是空那么不存在
//            return list.get(0).getId();
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_EXIST);

        }

        Member member=new Member();
        member.setId(SnowUtil.getSnowflakeNextId());
        member.setMobile(mobile);

        memberMapper.insert(member);
        return member.getId();
    }



//发送验证码
    public void sendCode(MemberSendCodeReq req) {
        String mobile=req.getMobile();
       Member memberDB = selectByMobile(mobile);//memberDB是从数据库查出来的
//        如果手机号不存在，则插入记录
        if (ObjectUtil.isNull(memberDB)){//判断数据库查出来的是不是空，如果是空那么不存在
            LOG.info("手机号不存在，插入记录");
            Member member=new Member();
            member.setId(SnowUtil.getSnowflakeNextId());
            member.setMobile(mobile);
            memberMapper.insert(member);
        }else {
            LOG.info("手机号存在，不插入记录");
        }
//生成验证码
//       String code =RandomUtil.randomString(4);
        String code ="8888";//方便测试将短信验证码改为8888
        LOG.info("生成短信验证码：{}",code);
//保存短信记录表：手机号，短信验证码，有效期，是否已使用，业务类型，发送时间，使用时间
        LOG.info("保存短信记录表");
//      对接短信通道，发送短信
        LOG.info("对接短信通道");
    }

//登录
    public MemberLoginResp login(MemberLoginReq req) {
        String mobile=req.getMobile();
        String code=req.getCode();
        Member memberDB = selectByMobile(mobile);//memberDB是从数据库查出来的
//        如果手机号不存在，则插入记录
        if (ObjectUtil.isNull(memberDB)){//判断数据库查出来的是不是空，如果是空那么不存在
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_NOT_EXIST);
        }
//        校验短信验证码
        if (!"8888".equals(code)) {
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_CODE_ERROR);
        }
        MemberLoginResp memberLoginResp = BeanUtil.copyProperties(memberDB,MemberLoginResp.class);//使用hutil的工具复制一个MemberLoginResp对象
//        避免将整个DB对象传给前端 造成用户重要信息(密码)等信息的泄露。
        return memberLoginResp;

    }

    private Member selectByMobile(String mobile) {
        MemberExample memberExample = new MemberExample();//创建一个Example
        memberExample.createCriteria().andMobileEqualTo(mobile);//将相同值的mobile赋给example
        List<Member> list = memberMapper.selectByExample(memberExample);//将查找到的相同的mobile存入list中
        if (CollUtil.isEmpty(list)){
        return null;
        }else {
            return list.get(0);
        }

    }
}
