package com.laisen.autojob.core.controller;

import com.laisen.autojob.quartz.entity.QuartzBean;
import com.laisen.autojob.quartz.util.QuartzUtils;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quartz/")
public class QuartzController {
    //注入任务调度
    @Autowired
    private Scheduler scheduler;

    @RequestMapping("/createJob")
    @ResponseBody
    public String createJob(QuartzBean quartzBean) {
        try {
            //进行测试所以写死
            quartzBean.setJobClass("com.laisen.autojob.quartz.EverPhotoJob");
            quartzBean.setJobName("test1");
            quartzBean.setCronExpression("*/10 * * * * ?");
            QuartzUtils.createScheduleJob(scheduler, quartzBean);
        } catch (Exception e) {
            return "创建失败";
        }
        return "创建成功";
    }

    @RequestMapping("/pauseJob")
    @ResponseBody
    public String pauseJob() {
        try {
            QuartzUtils.pauseScheduleJob(scheduler, "test1");
        } catch (Exception e) {
            return "暂停失败";
        }
        return "暂停成功";
    }

    @RequestMapping("/runOnce")
    @ResponseBody
    public String runOnce() {
        try {
            QuartzUtils.runOnce(scheduler, "test1");
        } catch (Exception e) {
            return "运行一次失败";
        }
        return "运行一次成功";
    }

    @RequestMapping("/resume")
    @ResponseBody
    public String resume() {
        try {

            QuartzUtils.resumeScheduleJob(scheduler, "test1");
        } catch (Exception e) {
            return "启动失败";
        }
        return "启动成功";
    }

    @RequestMapping("/update")
    @ResponseBody
    public String update(QuartzBean quartzBean) {
        try {
            //进行测试所以写死
            quartzBean.setJobClass("com.laisen.autojob.quartz.EverPhotoJob");
            quartzBean.setJobName("test1");
            quartzBean.setCronExpression("10 * * * * ?");
            QuartzUtils.updateScheduleJob(scheduler, quartzBean);
        } catch (Exception e) {
            return "启动失败";
        }
        return "启动成功";
    }
}