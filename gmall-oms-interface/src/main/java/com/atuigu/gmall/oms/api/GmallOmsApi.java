package com.atuigu.gmall.oms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atuigu.gmall.oms.entity.OrderEntity;
import com.atuigu.gmall.oms.vo.OrderSubmitVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface GmallOmsApi {

    @PostMapping("oms/order/create/{userId}")
    public ResponseVo<OrderEntity> addOrder(@RequestBody OrderSubmitVo submitVo, @PathVariable("userId")Long userId);

    @GetMapping("oms/order/token/{orderToken}")
    public ResponseVo<OrderEntity> queryOrderByToken(@PathVariable("orderToken")String orderToken);
}


