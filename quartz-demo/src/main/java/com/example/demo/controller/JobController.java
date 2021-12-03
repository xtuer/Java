package com.example.demo.controller;

import com.example.demo.bean.MyJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class JobController {
    @Autowired
    private Scheduler scheduler;

    /**
     * 执行任务
     *
     * 网址: http://localhost:8080/api/jobs/start
     * 参数: jobName: 任务的名字
     *
     * @param jobName 任务名字
     */
    @GetMapping("/api/jobs/start")
    public boolean startJob(@RequestParam String jobName) throws SchedulerException {
        // 1. 创建任务
        // 2. 立即执行任务
        //
        JobKey jobKey = JobKey.jobKey(jobName, "HELLO" + System.currentTimeMillis());
        //
        // // [1] 创建任务
        JobDetail jobDetail = JobBuilder.newJob()
                .ofType(MyJob.class)
                .withIdentity(jobKey)
                .storeDurably()
                .usingJobData("name", "Alice") // 设置任务的参数
                .usingJobData("age", 33)
                .build();
        // scheduler.addJob(jobDetail, true);
        //
        // // [2] 立即执行任务
        // scheduler.triggerJob(jobKey);
        //
        System.out.println(Thread.currentThread());

        Date date = new Date();
        date.setTime(date.getTime() + 40000);

        // 4S 后只执行一次任务
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger1", "group")
                .startAt(date)
                .usingJobData("gender", 1)
                .build();

        scheduler.scheduleJob(jobDetail, trigger);

        return true;
    }
}
