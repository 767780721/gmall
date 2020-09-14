package com.atguigu.gmall.scheduled.jobhandler;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.scheduled.mapper.CartMapper;
import com.atguigu.gmall.scheduled.pojo.Cart;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class CartJobHandler {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CartMapper cartMapper;

    private static final String KEY = "cart:async:exception";
    private static final String KEY_PREFIX = "cart:info:";

    @XxlJob("cartJobHandler")
    public ReturnT<String> tongBu(String param){
        BoundListOperations<String, String> listOps = redisTemplate.boundListOps(KEY);
        if(listOps.size() == 0){
            return ReturnT.SUCCESS;
        }

        String userId = listOps.rightPop();
        while (StringUtils.isNotBlank(userId)){
            //删除该用户mysql中所有的购物车
            cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id",userId));

            //获取该用户redis中的购物车
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
            List<Object> cartJsons = hashOps.values();
            if(CollectionUtils.isEmpty(cartJsons)){
                userId = listOps.rightPop();
                continue;
            }

            //添加到mysql中
            cartJsons.forEach(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                cartMapper.insert(cart);
            });

            userId = listOps.rightPop();
        }

        return ReturnT.SUCCESS;
    }
}
