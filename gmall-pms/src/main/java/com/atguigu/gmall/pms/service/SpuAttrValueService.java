package com.atguigu.gmall.pms.service;

import com.atguigu.gamll.pms.entity.SpuAttrValueEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import java.util.List;


/**
 * spu属性值
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-08-21 17:46:22
 */
public interface SpuAttrValueService extends IService<SpuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SpuAttrValueEntity> querySpuAttrValuesBySpuId(Long spuId);
}

