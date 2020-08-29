package com.atguigu.gmall.pms.service;

import com.atguigu.gamll.pms.entity.SkuImagesEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * sku图片
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-08-21 17:46:22
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

