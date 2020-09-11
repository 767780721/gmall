package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CartAsyncService {

    @Autowired
    private CartMapper cartMapper;

    @Async
    public void updateByUserIdAndSkuId(Cart cart){
        cartMapper.update(cart,new UpdateWrapper<Cart>()
                .eq("user_id",cart.getUserId())
                .eq("sku_id",cart.getSkuId()));
    }

    @Async
    public void addCart(Cart cart){
        cartMapper.insert(cart);
    }

    @Async
    public void deleteCateByUserId(String userKey) {
        cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id",userKey));
    }

    @Async
    public void deleteCateByUserIdAndSkuId(String userId,Long skuId){
        cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id",userId).eq("sku_id",skuId));
    }
}
