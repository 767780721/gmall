package com.atguigu.gmall.index.config;


import org.springframework.transaction.TransactionDefinition;

import java.lang.annotation.*;

/**
 * 模仿声明式事务，封装的声明式缓存注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {

    /**
     * 缓存前缀
     * @return
     */
    String prefix() default "cache";

    /**
     * 过期时间 默认是60分钟
     * 单位：分钟
     */
    int timeout() default 60;

    /**
     * 为了防止缓存雪崩,给缓存时间设置随机值
     * 单位:分钟
     *
     * @return
     */
    int random() default 10;

    /**
     * 为了防止缓存击穿，给缓存去指定分布式锁的名称
     * @return
     */
    String lock() default "lock";


}
