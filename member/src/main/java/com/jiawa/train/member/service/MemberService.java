package com.jiawa.train.member.service;

import cn.hutool.core.collection.CollUtil;
import com.jiawa.train.member.domain.Member;
import com.jiawa.train.member.domain.MemberExample;
import com.jiawa.train.member.mapper.MemberMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {
    @Resource
    private MemberMapper memberMapper;
    public int count(){
        return Math.toIntExact(memberMapper.countByExample(null));
    }


    public long register(String mobile) {
        MemberExample memberExample = new MemberExample();//创建一个Example
        memberExample.createCriteria().andMobileEqualTo(mobile);//将相同值的mobile赋给example
        List<Member> list = memberMapper.selectByExample(memberExample);//将查找到的相同的mobile存入list中
        if (CollUtil.isNotEmpty(list)){
//            return list.get(0).getId();
            throw new RuntimeException("手机号已注册");

        }

        Member member=new Member();
        member.setId(System.currentTimeMillis());
        member.setMobile(mobile);

        memberMapper.insert(member);
        return member.getId();
    }
}
