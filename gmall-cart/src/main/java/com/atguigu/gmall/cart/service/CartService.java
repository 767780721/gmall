package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gamll.pms.api.GmallPmsApi;
import com.atguigu.gamll.pms.entity.SkuAttrValueEntity;
import com.atguigu.gamll.pms.entity.SkuEntity;
import com.atguigu.gamll.wms.entity.WareSkuEntity;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.CartException;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Service
public class CartService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private CartAsyncService cartAsyncService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private final static ObjectMapper MAPPER = new ObjectMapper();//也可以使用

    private static final String KEY_PREFIX = "cart:info:";

    public void addCart(Cart cart) {
        //1.组装key
        String userId = getUserId();
        String key = KEY_PREFIX + userId;

        //2.获取该用户的购物车
        //可以直接操作内层的map结构  hashOps相当于内层的 Map<skuId字符串,cart的json字符串>
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);

        //3.判断该用户是否已有该购物车记录
        String skuId = cart.getSkuId().toString();
        BigDecimal count = cart.getCount();//用户添加购物车商品的数量
        if(hashOps.hasKey(skuId)){
            //有则更新数量
            String cartJson = hashOps.get(skuId).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(cart.getCount().add(count));
            //写回数据库
            cartAsyncService.updateByUserIdAndSkuId(cart);
        } else {
            //无则新增新的记录
            cart.setUserId(userId);
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if(skuEntity == null){
                throw new CartException("您加入购物车的商品不存在...");
            }
            cart.setTitle(skuEntity.getTitle());
            cart.setPrice(skuEntity.getPrice());
            cart.setDefaultImage(skuEntity.getDefaultImage());
            //根据skuId查询销售属性
            ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResponseVo = pmsClient.querySaleAttrValuesBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueResponseVo.getData();
            cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));

            //根据skuId查询营销信息
            ResponseVo<List<ItemSaleVo>> itemSaleResponseVo = smsClient.querySaleVoBySkuId(cart.getSkuId());
            List<ItemSaleVo> itemSaleVos = itemSaleResponseVo.getData();
            cart.setSales(JSON.toJSONString(itemSaleVos));

            //根据skuId查询库存信息
            ResponseVo<List<WareSkuEntity>> wareResponseVo = wmsClient.queryWareSkusBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
            if(!CollectionUtils.isEmpty(wareSkuEntities)){
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }

            //选中状态
            cart.setCheck(true);
            cartAsyncService.addCart(cart);
        }
        hashOps.put(skuId,JSON.toJSONString(cart));

    }

    public Cart queryCartBySkuId(Long skuId) throws JsonProcessingException {
        //1.获取登录信息
        String userId = getUserId();
        String key = KEY_PREFIX + userId;

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        if(hashOps.hasKey(skuId.toString())){
            String cartJson = hashOps.get(skuId.toString()).toString();
//            return JSON.parseObject(cartJson,Cart.class);
            return MAPPER.readValue(cartJson,Cart.class);
        }

       throw new RuntimeException("你的购物车中没有该商品记录");
    }

    private String getUserId(){
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if(userInfo.getUserId() != null){
            //如果用户的id不为空，说明用户已登录，添加购物车应该以userId作为key
            return userInfo.getUserId().toString();
        }
        //未登录 以userKey作为key
        return userInfo.getUserKey();
    }


    @Async
    public void executor1(){
        try {
            System.out.println("异步方法executor1开始执行" + Thread.currentThread().getName());
            TimeUnit.SECONDS.sleep(3);
            System.out.println("异步方法executor1结束执行");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    public void executor2(){
        try {
            System.out.println("异步方法executor2开始执行" + Thread.currentThread().getName());
            TimeUnit.SECONDS.sleep(4);
            int i = 1/0;
            System.out.println("异步方法executor2结束执行");
        } catch (InterruptedException e) {
            System.out.println("service方法捕获异常后的打印: " + e.getMessage());
        }
    }

  /*  @Async
    public ListenableFuture<String> executor1(){
        try {
            System.out.println("异步方法executor1开始执行");
            TimeUnit.SECONDS.sleep(3);
            System.out.println("异步方法executor1结束执行");
        } catch (Exception e) {
            e.printStackTrace();
            return AsyncResult.forExecutionException(e);
        }
        return AsyncResult.forValue("hello executor1");
    }

    @Async
    public ListenableFuture<String> executor2(){
        try {
            System.out.println("异步方法executor2开始执行");
            TimeUnit.SECONDS.sleep(4);
            int i = 1/0;
            System.out.println("异步方法executor2结束执行");
        } catch (Exception e) {
            System.out.println("service方法捕获异常后的打印: " + e.getMessage());
            return AsyncResult.forExecutionException(e);
        }
        return AsyncResult.forValue("hello executor2");
    }*/


}
