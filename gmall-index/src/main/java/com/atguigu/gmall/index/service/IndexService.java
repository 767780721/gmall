package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gamll.pms.entity.CategoryEntity;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.config.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.utils.DistributedLock;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IndexService {

    @Autowired
    private GmallPmsClient pmsClient;

    private static final String KEY_PREFIX = "index:cates:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DistributedLock distributedLock;

    @Autowired
    private RedissonClient redissonClient;

    public List<CategoryEntity> queryLevelOneCategories() {
        ResponseVo<List<CategoryEntity>> listResponseVo = pmsClient.queryCategoriesByPid(0L);
        return listResponseVo.getData();
    }

    @GmallCache(prefix = KEY_PREFIX, timeout = 43200, random = 7200, lock = "lock")
    @Transactional
    public List<CategoryEntity> queryLevelTwoCategoriesWithSub(Long pid) {
        ResponseVo<List<CategoryEntity>> listResponseVo = pmsClient.queryCategoriesWithSubByPid(pid);
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        return categoryEntities;
    }

    public List<CategoryEntity> queryLevelTwoCategoriesWithSub2(Long pid) {
        //1.先查询缓存
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if(StringUtils.isNotBlank(json)){
            List<CategoryEntity> categoryEntities = JSON.parseArray(json, CategoryEntity.class);
            return categoryEntities;
        }
        //分布式锁
        RLock lock = redissonClient.getLock("lock:" + pid);
        lock.lock();

        //再次判断缓存中是否有
        String json2 = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if(StringUtils.isNotBlank(json)){
            List<CategoryEntity> categoryEntities = JSON.parseArray(json, CategoryEntity.class);
            lock.unlock();
            return categoryEntities;
        }

        //2.缓存没有，远程调用，查询数据库，并放入缓存
        ResponseVo<List<CategoryEntity>> listResponseVo = pmsClient.queryCategoriesWithSubByPid(pid);
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        redisTemplate.opsForValue().set(KEY_PREFIX + pid,JSON.toJSONString(categoryEntities),30, TimeUnit.DAYS);

        lock.unlock();
        return categoryEntities;
    }

    public void testLock() {
        RLock lock = redissonClient.getFairLock("lock");
        lock.lock();
        //获取成功 则执行业务操作
        //查询redis中的num值
        String value = redisTemplate.opsForValue().get("num");
        //没有该值 设置后return
        if(StringUtils.isBlank(value)){
            this.redisTemplate.opsForValue().set("num","0");
            return;
        }
        //有值就转成int
        int num = Integer.parseInt(value);
        redisTemplate.opsForValue().set("num",String.valueOf(++num));
        lock.unlock();
    }

    public void testLock3() {
        //通过setnx来获取锁
        String uuid = UUID.randomUUID().toString();
        Boolean lock = distributedLock.tryLock("lock", uuid, 30L);
        if(lock){
            //获取成功 执行业务操作
            //查询redis中的num值
            String value = redisTemplate.opsForValue().get("num");
            //没有该值 设置后return
            if(StringUtils.isBlank(value)){
                this.redisTemplate.opsForValue().set("num","0");
                return;
            }
            try {
                TimeUnit.SECONDS.sleep(60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //有值就转成int
            int num = Integer.parseInt(value);
            redisTemplate.opsForValue().set("num",String.valueOf(++num));
        }
        testSubLock(uuid);
        distributedLock.unlock("lock",uuid);
    }



    public void testSubLock(String uuid){
        Boolean lock = distributedLock.tryLock("lock", uuid, 30L);
        System.out.println("=================");
        distributedLock.unlock("lock",uuid);
    }

    public void testLock2() {
        //通过setnx来获取锁
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid,3,TimeUnit.SECONDS);
        //获取失败重试`
        if(!lock){
            try {
                Thread.sleep(100);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            //获取成功 执行业务操作
            //查询redis中的num值
            String value = redisTemplate.opsForValue().get("num");
            //没有该值return
            if(StringUtils.isBlank(value)){
                this.redisTemplate.opsForValue().set("num","0");
                return;
            }
            //有值就转成int
            int num = Integer.parseInt(value);
            redisTemplate.opsForValue().set("num",String.valueOf(++num));
            //释放锁
            //为了防误删 需要判断当前锁是不是自己的锁
            //为了保证原子性，这里使用lua脚本
            String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
            redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class), Arrays.asList("lock"),uuid);


//            if(StringUtils.equals(uuid,redisTemplate.opsForValue().get("lock"))){
//                redisTemplate.delete("lock");
//            }
        }
    }

    public void testRead() {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rwLock");
        rwLock.readLock().lock(10,TimeUnit.SECONDS);
        System.out.println("读操作");
    }

    public void testWrite() {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rwLock");
        rwLock.writeLock().lock(10,TimeUnit.SECONDS);
        System.out.println("写操作");
    }

    public void testCoundDown() {
        RCountDownLatch latch = redissonClient.getCountDownLatch("latch");
        latch.countDown();
    }

    public void testLatch() {
        RCountDownLatch latch = redissonClient.getCountDownLatch("latch");
        try {
            latch.trySetCount(6L);
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
