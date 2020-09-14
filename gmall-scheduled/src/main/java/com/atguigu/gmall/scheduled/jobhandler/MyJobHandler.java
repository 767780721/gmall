package com.atguigu.gmall.scheduled.jobhandler;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.stereotype.Component;

@Component
public class MyJobHandler {

    /**
     * 方法结构固定写法
     * 通过XxlJob指定方法是一个任务,并给任务指定一个唯一名称
     * 通过XxlJobLogger向调度中心输出日志
     * @param param
     * @return
     */
    @XxlJob("MyJobHandler")
    public ReturnT<String> test(String param){
        XxlJobLogger.log("this is MyJobHandler log : " + param);
        System.out.println("这是我的第一个xxl任务" + System.currentTimeMillis() + ", 调度中心可以传递一个param参数: " + param);
        return ReturnT.SUCCESS;
    }
}
