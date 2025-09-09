package com.jiawa.train.${module}.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.${module}.domain.${Domain};
import com.jiawa.train.${module}.domain.${Domain}Example;
import com.jiawa.train.${module}.mapper.${Domain}Mapper;
import com.jiawa.train.${module}.req.${Domain}QueryReq;
import com.jiawa.train.${module}.req.${Domain}SaveReq;
import com.jiawa.train.${module}.resp.${Domain}QueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ${Domain}Service {
      public static final Logger LOG = LoggerFactory.getLogger(${Domain}Service.class);
    @Resource
    private ${Domain}Mapper ${domain}Mapper;
    public void save(${Domain}SaveReq req){
        DateTime now = DateTime.now();
        // 将请求对象req的属性复制到${Domain}对象中（需要确保两个类的属性名和类型匹配）
        ${Domain} ${domain} = BeanUtil.copyProperties(req, ${Domain}.class);
        if (ObjectUtil.isNull(${domain}.getId())){/*根据id判断是新增保存还是编辑保存*/
            /*新增保存*/
            ${domain}.setMemberId(LoginMemberContext.getId());//拦截器获取headers数据，得到用户id
            ${domain}.setId(SnowUtil.getSnowflakeNextId());
            ${domain}.setCreateTime(now);
            ${domain}.setUpdateTime(now);
            ${domain}Mapper.insert(${domain});
        }else {/*编辑保存*/
            ${domain}.setUpdateTime(now);
            ${domain}Mapper.updateByPrimaryKey(${domain});
        }

    }

    public PageResp<${Domain}QueryResp> queryList(${Domain}QueryReq req){
        ${Domain}Example ${domain}Example = new ${Domain}Example();// 创建MyBatis的Example查询对象
        ${domain}Example.setOrderByClause("id desc");
        ${Domain}Example.Criteria criteria = ${domain}Example.createCriteria();    // 创建查询条件Criteria对象
        if (ObjectUtil.isNotNull(req.getMemberId())){// 条件判断：如果请求参数中的会员ID不为空，则添加会员ID等于条件
            criteria.andMemberIdEqualTo(req.getMemberId());
        } // 执行查询，获取乘客实体列表
        /*在${Domain}QueryReq req传入的参数中包含页码和页数*/
        LOG.info("查询页码：{}",req.getPage());
        LOG.info("每条页数：{}",req.getSize());
        /*分页组件*/
        PageHelper.startPage(req.getPage(), req.getSize());
        /*执行分页查询，得到的是“当前页”的数据列表，List大小 <= req.getSize()*/
        List<${Domain}> ${domain}List = ${domain}Mapper.selectByExample(${domain}Example);
        // 将实体列表转换为响应对象列表（DTO转换）
        List<${Domain}QueryResp> List = BeanUtil.copyToList(${domain}List, ${Domain}QueryResp.class);
        /*获取包含“总记录数”和“当前页数据”的分页信息对象*/
        PageInfo<${Domain}> pageInfo = new PageInfo<>(${domain}List);
        LOG.info("总行数：{}",pageInfo.getTotal());
        LOG.info("总页数：{}",pageInfo.getPages());
       /* 创建自定义的响应对象 PageResp*/
        PageResp<${Domain}QueryResp> pageResp = new PageResp<>();
        /*将“总记录数”和“当前页的DTO列表”设置到自定义响应对象中*/
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(List);
        return pageResp;
    }


    public void delete(Long id){
        ${domain}Mapper.deleteByPrimaryKey(id);
    }

}