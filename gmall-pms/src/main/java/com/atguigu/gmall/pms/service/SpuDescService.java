package com.atguigu.gmall.pms.service;

import com.atguigu.gamll.pms.entity.SpuDescEntity;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;


/**
 * spu信息介绍
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-08-21 17:46:22
 */
public interface SpuDescService extends IService<SpuDescEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    void saveSpuDesc(SpuVo spuVo, Long spuId);
}

