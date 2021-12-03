package com.example.demo.bean;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class MyJob implements Job {
    public MyJob() {
        System.out.println("MyJob: " + this);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("executing job...");
        System.out.println(Thread.currentThread());

        // 获取任务的参数
        JobDataMap data = context.getMergedJobDataMap(); // 来自于 JobDetails 和 Trigger 的 dataMap
        data.forEach((key, value) -> {
            System.out.printf("key: %s, value: %s\n", key, value);
        });
    }
}
