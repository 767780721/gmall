package com.atuigu.gmall.oms.vo;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 提交订单需要携带的信息
 */
@Data
public class OrderSubmitVo {

    private String orderToken; // 防重复提交

    private BigDecimal totalPrice; // 总价，校验价格变化

    private UserAddressEntity address; // 收货地址信息

    private Integer payType; // 支付方式

    private String deliveryCompany; // 配送方式

    private List<OrderItemVo> items; // 订单详情信息

    private Integer bounds; // 积分信息
}
