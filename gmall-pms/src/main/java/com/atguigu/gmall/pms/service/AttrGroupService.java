package com.atguigu.gmall.pms.service;

import com.atguigu.gamll.pms.entity.AttrGroupEntity;
import com.atguigu.gamll.pms.vo.GroupVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;


import java.util.List;

/**
 * 属性分组
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-08-21 17:46:22
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageResultVo queryPage(PageParamVo paramVo);


    List<AttrGroupEntity> queryGroupWithAttrsByCatId(Long catId);

    List<GroupVo> queryGroupVoByCidAndSpuIdAndSkuId(Long cid, Long spuId, Long skuId);
}

