package com.atguigu.gmall.oms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atuigu.gmall.oms.entity.OrderSettingEntity;

/**
 * 订单配置信息
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-09-12 18:41:18
 */
public interface OrderSettingService extends IService<OrderSettingEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

