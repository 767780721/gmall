package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.exception.CartException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 新增购物车
     * 新增成功之后重定向到回显页面
     * @return
     */
    @GetMapping
    public String addCart(Cart cart){
        if(cart == null || cart.getSkuId() == null){
            throw new CartException("请选择加入购物车的商品!");
        }
        cartService.addCart(cart);
        return "redirect:http://cart.gmall.com/addCart.html?skuId=" + cart.getSkuId();
    }

    @GetMapping("addCart.html")
    public String queryCartBySkuId(@RequestParam("skuId") Long skuId, Model model) throws JsonProcessingException {
        Cart cart = cartService.queryCartBySkuId(skuId);
        model.addAttribute("cart",cart);
        return "addCart";
    }

 /*   @GetMapping("test")
    @ResponseBody
    public String test(HttpServletRequest request){
//        System.out.println(request.getAttribute("userId"));
        System.out.println(LoginInterceptor.getUserInfo());
        return "测试一下拦截器";
    }*/

    @GetMapping("test")
    @ResponseBody
    public String test(HttpServletRequest request){
        long now = System.currentTimeMillis();
        System.out.println("controller方法开始执行==========");
        cartService.executor1();
        cartService.executor2();
      /*  ListenableFuture<String> future1 = cartService.executor1();
        ListenableFuture<String> future2 = cartService.executor2();
        future1.addCallback(t -> System.out.println("controller方法获取了future1的返回结果集"  + t),
                ex -> System.out.println("controller方法获取了future1的异常信息" + ex.getMessage()));
        future2.addCallback(t -> System.out.println("controller方法获取了future2的返回结果集"  + t),
                ex -> System.out.println("controller方法获取了future2的异常信息" + ex.getMessage()));*/
       /* try {
            System.out.println(future1.get());
            System.out.println("controller手动打印: " + future2.get());
        } catch (Exception e) {
            System.out.println("controller捕获异常后的打印: " + e.getMessage());
        }*/
        System.out.println("controller方法结束执行==========" + (System.currentTimeMillis() - now));
        return "hello test";
    }

}
