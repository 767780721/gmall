package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.mapper.SkuFullReductionMapper;
import com.atguigu.gmall.sms.mapper.SkuLadderMapper;
import com.atguigu.gmall.sms.vo.SkuSalesVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.sms.mapper.SkuBoundsMapper;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.util.CollectionUtils;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsMapper, SkuBoundsEntity> implements SkuBoundsService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuBoundsEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageResultVo(page);
    }

    @Autowired
    private SkuFullReductionMapper skuFullReductionMapper;

    @Autowired
    private SkuLadderMapper skuLadderMapper;

    @Override
    public void saveSkuSales(SkuSalesVo skuSalesVo) {
        //3.优惠信息表保存
        //3.1 sms_sku_bounds
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(skuSalesVo,skuBoundsEntity);
        List<Integer> works = skuSalesVo.getWork();
        if(!CollectionUtils.isEmpty(works)){
            skuBoundsEntity.setWork(works.get(0) + works.get(1)*2 + works.get(2)*4 + works.get(3)*8);
        }
        skuBoundsEntity.setId(null);
        save(skuBoundsEntity);

        //3.2 sms_sku_full_reduction
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuSalesVo,skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(skuSalesVo.getFullAddOther());
        skuFullReductionEntity.setId(null);
        skuFullReductionMapper.insert(skuFullReductionEntity);

        //3.3 sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuSalesVo,skuLadderEntity);
        skuLadderEntity.setAddOther(skuSalesVo.getLadderAddOther());
        skuLadderEntity.setId(null);
        skuLadderMapper.insert(skuLadderEntity);

    }

}