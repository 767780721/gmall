package com.atguigu.gmall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gamll.wms.entity.WareOrderBillEntity;

/**
 * 库存工作单
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-08-23 20:55:44
 */
public interface WareOrderBillService extends IService<WareOrderBillEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

