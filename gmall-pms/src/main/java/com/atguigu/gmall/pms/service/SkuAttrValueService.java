package com.atguigu.gmall.pms.service;

import com.atguigu.gamll.pms.entity.SkuAttrValueEntity;
import com.atguigu.gamll.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import java.util.List;


/**
 * sku销售属性&值
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-08-21 17:46:22
 */
public interface SkuAttrValueService extends IService<SkuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SkuAttrValueEntity> querySearchAttrValuesBySkuId(Long skuId);

    List<SaleAttrValueVo> querySaleAttrVoBySpuId(Long spuId);

    String querySaleAttrValuesMappingSkuIdBySpuId(Long spuId);
}


