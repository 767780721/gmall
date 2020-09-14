package com.atguigu.gmall.wms.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gamll.wms.vo.SkuLockVo;
import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class StockListener {

    @Autowired
    private WareSkuMapper wareSkuMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "stock:lock:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "STOCK_UNLOCK_QUEUE",durable = "true"),
            exchange = @Exchange(value = "ORDER_EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"order.failure","stock.dead"}
    ))
    public void unlock(String orderToken, Channel channel, Message message) throws IOException {
        //判断orderToken是否为空
        if(StringUtils.isBlank(orderToken)){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }
        //获取redis中该订单锁定库存的信息
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + orderToken);
        if(StringUtils.isNotBlank(json)){
            //反序列化获取库存的锁定信息
            List<SkuLockVo> skuLockVos = JSON.parseArray(json, SkuLockVo.class);
            //遍历并解锁库存信息
            skuLockVos.forEach(skuLockVo -> {
                wareSkuMapper.unLock(skuLockVo.getWareSkuId(),skuLockVo.getCount());
            });
            //删除redis中的库存锁定信息
            redisTemplate.delete(KEY_PREFIX + orderToken);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);

    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "STOCK_MINUS_QUEUE",durable = "true"),
            exchange = @Exchange(value = "ORDER_EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"stock.minus"}
    ))
    public void minus(String orderToken, Channel channel, Message message) throws IOException {
        //判断orderToken是否为空
        if(StringUtils.isBlank(orderToken)){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }
        //获取redis中该订单锁定库存的信息
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + orderToken);
        if(StringUtils.isNotBlank(json)){
            //反序列化获取库存的锁定信息
            List<SkuLockVo> skuLockVos = JSON.parseArray(json, SkuLockVo.class);
            //遍历并解锁库存信息
            skuLockVos.forEach(skuLockVo -> {
                wareSkuMapper.minus(skuLockVo.getWareSkuId(),skuLockVo.getCount());
            });
            //删除redis中的库存锁定信息
            redisTemplate.delete(KEY_PREFIX + orderToken);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);

    }
}
