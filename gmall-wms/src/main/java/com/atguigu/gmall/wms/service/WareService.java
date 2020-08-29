package com.atguigu.gmall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gamll.wms.entity.WareEntity;

/**
 * 仓库信息
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-08-23 20:55:44
 */
public interface WareService extends IService<WareEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

