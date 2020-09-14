package com.atguigu.gmall.oms.mapper;

import com.atuigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单
 * 
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-09-12 18:41:18
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {

    public int updateStatus(
            @Param("orderToken") String orderToken,
            @Param("newStatus") Integer newStatus,
            @Param("oldStatus")Integer oldStatus);
	
}
