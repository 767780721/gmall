package com.atguigu.gmall.order.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gamll.pms.entity.SkuAttrValueEntity;
import com.atguigu.gamll.pms.entity.SkuEntity;
import com.atguigu.gamll.wms.entity.WareSkuEntity;
import com.atguigu.gamll.wms.vo.SkuLockVo;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.UserInfo;
import com.atguigu.gmall.common.exception.OrderException;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atuigu.gmall.oms.entity.OrderEntity;
import com.atuigu.gmall.oms.vo.OrderItemVo;
import com.atuigu.gmall.oms.vo.OrderSubmitVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import springfox.documentation.spring.web.json.Json;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private GmallCartClient cartClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallOmsClient omsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String TOKEN_PREFIX = "order:token:";

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 订单确认页
     * 由于存在大量的远程调用，这里使用异步编排做优化
     * @return
     */
    public OrderConfirmVo confirm() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        //获取用户的登录信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();

        //查询送货清单
        ResponseVo<List<Cart>> cartResponseVo = cartClient.queryCheckedCarts(userId);
        List<Cart> carts = cartResponseVo.getData();
        if(CollectionUtils.isEmpty(carts)){
            throw new OrderException("没有选中的购物车信息");
        }
        List<OrderItemVo> itemVos = carts.stream().map(cart -> {
            OrderItemVo itemVo = new OrderItemVo();
            itemVo.setSkuId(cart.getSkuId());
            itemVo.setCount(cart.getCount());

            //查询订单列表中sku相关的信息
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if(skuEntity != null){
                itemVo.setDefaultImage(skuEntity.getDefaultImage());
                itemVo.setPrice(skuEntity.getPrice());
                itemVo.setTitle(skuEntity.getTitle());
                itemVo.setWeight(new BigDecimal(skuEntity.getWeight()));
            }

            //查询销售属性
            ResponseVo<List<SkuAttrValueEntity>> skuAttrValuesResponseVo = pmsClient.querySaleAttrValuesBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValuesResponseVo.getData();
            itemVo.setSaleAttrs(skuAttrValueEntities);


            //查询营销信息
            ResponseVo<List<ItemSaleVo>> saleResponseVo = smsClient.querySaleVoBySkuId(cart.getSkuId());
            List<ItemSaleVo> itemSaleVos = saleResponseVo.getData();
            itemVo.setSales(itemSaleVos);


            //查询库存
            ResponseVo<List<WareSkuEntity>> waresResponseVo = wmsClient.queryWareSkusBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = waresResponseVo.getData();
            if(!CollectionUtils.isEmpty(wareSkuEntities)){
                itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }

            return itemVo;
        }).collect(Collectors.toList());
        confirmVo.setOrderItems(itemVos);

        //查询收货地址列表
        ResponseVo<List<UserAddressEntity>> userAddressResponseVo = umsClient.queryUserAddressByUserId(userId);
        List<UserAddressEntity> addresses = userAddressResponseVo.getData();
        confirmVo.setAddresses(addresses);

        //查询用户的积分信息
        ResponseVo<UserEntity> userEntityResponseVo = umsClient.queryUserById(userId);
        UserEntity userEntity = userEntityResponseVo.getData();
        confirmVo.setBounds(userEntity.getIntegration());

        //防重的唯一标识,响应给页面一份，保存到redis中一份
        String orderToken = IdWorker.getTimeId();
        confirmVo.setOrderToken(orderToken);
        redisTemplate.opsForValue().set(TOKEN_PREFIX + orderToken,orderToken);

        return confirmVo;
    }

    public OrderEntity submit(OrderSubmitVo submitVo) {
//        1.防重：判断是否重复提交 通过redis完成\
        String orderToken = submitVo.getOrderToken();
        if(StringUtils.isEmpty(orderToken)){
            throw new OrderException("非法请求");
        }
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        Boolean flag = redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(TOKEN_PREFIX + orderToken), orderToken);
        if(!flag){
            throw new OrderException("请不要重复提交");
        }


//        2.验总价：获取页面上的总价，和数据库中的商品实时价格是否一致,将来根据OrderItems中的skuId查询sku即可
        BigDecimal totalPrice = submitVo.getTotalPrice();
        if(totalPrice == null){
            throw new OrderException("非法请求");
        }
        List<OrderItemVo> items = submitVo.getItems();
        if(CollectionUtils.isEmpty(items)){
            throw new OrderException("请选择要购买的商品！");
        }
        BigDecimal currentTotalPrice = items.stream().map(item -> {
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(item.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity != null) {
                return skuEntity.getPrice().multiply(item.getCount());
            }
            return new BigDecimal(0);
        }).reduce((a, b) -> a.add(b)).get();
        if(totalPrice.compareTo(currentTotalPrice) != 0){
            throw new OrderException("页面已过期，请刷新后重试");
        }
//        3.验库存并锁库存
        List<SkuLockVo> skuLockVos = items.stream().map(item -> {
            SkuLockVo skuLockVo = new SkuLockVo();
            skuLockVo.setSkuId(item.getSkuId());
            skuLockVo.setCount(item.getCount().intValue());
            return skuLockVo;
        }).collect(Collectors.toList());
        ResponseVo<List<SkuLockVo>> lockResponseVo = wmsClient.checkAndLockStock(skuLockVos, orderToken);
        List<SkuLockVo> lockVos = lockResponseVo.getData();
        if(!CollectionUtils.isEmpty(lockVos)){
            throw new OrderException(JSON.toJSONString(lockVos));
        }
//        int i = 1/0;
//        4.创建订单并添加订单详情
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        OrderEntity orderEntity = null;
        try {
            ResponseVo<OrderEntity> orderEntityResponseVo = omsClient.addOrder(submitVo, userId);
            orderEntity = orderEntityResponseVo.getData();
            //延时队列解锁库存
            rabbitTemplate.convertAndSend("ORDER_EXCHANGE","order.ttl",orderToken);
        } catch (Exception e) {
            e.printStackTrace();
            //订单创建失败，立马解锁库存
            rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "order.failure", orderToken);
            throw new OrderException("服务器错误!");
        }


//        5.删除购物车中对应的商品记录,异步删除购物车
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
        map.put("skuIds",JSON.toJSONString(skuIds));
        rabbitTemplate.convertAndSend("ORDER_EXCHANGE","cart.delete",map);

        return orderEntity;
    }
}
