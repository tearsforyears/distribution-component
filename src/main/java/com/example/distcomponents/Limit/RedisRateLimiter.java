package com.example.distcomponents.Limit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhanghaoyang
 */
@Component
public class RedisRateLimiter {
    /**
     * 秒级限制器,实现思路就是每一个固定的秒是redis中的一个key,我们上界是1000次访问
     * 利用redis充当计数器,其中有两个点需要考虑,redis的
     * 超过访问直接返回失败,否则则尝试获取
     */

    @Autowired
    RedisTemplate redisTemplate;


    public Boolean tryAcquire(String usertoken) {
        // 有可能出现多个线程调用,使用ThreadLocal
        SimpleDateFormat df = ThreadSafeDateFormatter.dateFormatThreadLocal.get();
        String key = usertoken + " limit count " + df.format(new Date());
        long expired = 1000L + new Random().nextInt(1000);
        // 如果不存在就加入一个key
        try {
            redisTemplate.opsForValue().setIfAbsent(key, 0L, expired, TimeUnit.MILLISECONDS);
            // redis实现了一个分布式自增的类
            RedisAtomicInteger i = new RedisAtomicInteger(key, redisTemplate.getConnectionFactory());
            int val = i.getAndIncrement();
            if (val >= 1000) {
                // 超过1000的全部给false,此时数据库的值在增加,但接口全部不予以调用
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            // log
            e.printStackTrace();
            return null;
        }
    }
}

class ThreadSafeDateFormatter {
    public static ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };
}