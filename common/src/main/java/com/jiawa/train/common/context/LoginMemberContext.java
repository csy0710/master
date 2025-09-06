package com.jiawa.train.common.context;

import com.jiawa.train.common.resp.MemberLoginResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//获取线程变量
public class LoginMemberContext {
    private static final Logger LOG = LoggerFactory.getLogger(LoginMemberContext.class);

    private static ThreadLocal<MemberLoginResp> member = new ThreadLocal<>();//声明一个member线程，固定写法

    public static MemberLoginResp getMember() {
        return member.get();
    }//通过getMember拿到当前线程的member

    public static void setMember(MemberLoginResp member) {
        LoginMemberContext.member.set(member);
    }//通过set向线程本地变量中赋值

    public static Long getId() {//扩展方法，封装成getId
        try {
            return member.get().getId();
        } catch (Exception e) {
            LOG.error("获取登录会员信息异常", e);
            throw e;
        }
    }

}
