package com.example.distcomponents.controller;

import com.example.distcomponents.Limit.RedisRateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhanghaoyang
 */
@RestController
public class TestController {
    @Autowired
    RedisRateLimiter limiter;
    @GetMapping("/Main")
    public String Main() {
        // 模拟20个线程不断处理接口,这里用了线程池模拟,本接口仅作为函数入口
        ExecutorService pool = Executors.newFixedThreadPool(2000);
        int taskNum = 10000;
        AtomicInteger in = new AtomicInteger(0);
        for (int i = 0; i < taskNum; i++) {
            pool.submit(()->{
                if(limiter.tryAcquire("usertoken")){
                    System.out.println("放行调用接口");
                    // 调用接口
//                    System.out.println(in.getAndIncrement());
                    System.out.println(Thread.currentThread().getName()+"调用接口");
                }else{
//                    System.out.println(in.get()+"refuse");
                    System.out.println(Thread.currentThread().getName()+"限制访问,进行服务降级处理");
                }
            });
        }
        return "ok";
    }
}
