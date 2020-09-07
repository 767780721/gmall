package com.atguigu.gmall.index.config;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class GmallCacheAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 目标类: joinPoint.getTarget().getClass()
     * 目标方法签名: (Method)joinPoint.getSignature()
     * 目标方法形参: joinPoint.getArgs()
     * joinPoint.getArgs(); 获取方法参数
     * joinPoint.getTarget().getClass(); 获取目标类
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("@annotation(GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        //获取方法签名
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        //获取了目标方法
        Method method = signature.getMethod();
        //获取目标方法上的指定的注解
        GmallCache annotation = method.getAnnotation(GmallCache.class);
        Class<?> returnType = method.getReturnType();
        //获取注解中的缓存前缀
        String prefix = annotation.prefix();

        //获取了方法形参
        List<Object> args =  Arrays.asList(joinPoint.getArgs());
        //key = prefix + args
        String key = prefix + args;
        //查询缓存，如果有直接返回
        String json = redisTemplate.opsForValue().get(key);
        if(StringUtils.isNotBlank(json)){
            return JSON.parseObject(json,returnType);
        }

        //加分布式锁
        String lock = annotation.lock();
        RLock fairLock = redissonClient.getFairLock(lock + ":" + args);
        fairLock.lock();
        //再次查询缓存，如果命中zhijiefanhui
        String json2 = redisTemplate.opsForValue().get(key);
        if(StringUtils.isNotBlank(json2)){
            fairLock.unlock();
            return JSON.parseObject(json,returnType);
        }
        //缓存中没有，执行目标方法
        Object result = joinPoint.proceed(joinPoint.getArgs());

        //放入缓存,并释放分布式锁
        int random = annotation.random();
        int timeout = annotation.timeout() + new Random().nextInt(random);

        redisTemplate.opsForValue().set(key,JSON.toJSONString(result),timeout, TimeUnit.MINUTES);
        fairLock.unlock();
        return result;
    }
}
