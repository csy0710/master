package com.jiawa.train.business.controller.admin;

import com.jiawa.train.common.resp.CommonResp;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.business.req.DailyTrainQueryReq;
import com.jiawa.train.business.req.DailyTrainSaveReq;
import com.jiawa.train.business.resp.DailyTrainQueryResp;
import com.jiawa.train.business.service.DailyTrainService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/admin/daily-train")
public class DailyTrainAdminController {
    @Resource
    private DailyTrainService dailyTrainService;


    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody DailyTrainSaveReq req) {
        dailyTrainService.save(req);
        return new CommonResp<>();
    }

    @GetMapping ("/query-list")
    public CommonResp<PageResp<DailyTrainQueryResp>> queryList(@Valid DailyTrainQueryReq req) {
        PageResp<DailyTrainQueryResp> list = dailyTrainService.queryList(req);
        return new CommonResp<>(list);
    }
    @DeleteMapping("/delete/{id}")/*resful风格的请求*/
    public CommonResp<Object> delete(@PathVariable Long id) {/*@PathVariable注解，取@DeleteMapping("/delete/{id}")变量*/
        dailyTrainService.delete(id);
        return new CommonResp<>();
    }
    @GetMapping("/gen-daily/{date}")/*resful风格的请求*/
    public CommonResp<Object> delete(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {/*@PathVariable注解，取@DeleteMapping("/delete/{id}")变量*/
        dailyTrainService.genDaily(date);
        return new CommonResp<>();
    }

}
