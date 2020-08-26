package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSalesVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo spuByCidPage(Long cid, PageParamVo pageParamVo) {
        QueryWrapper<SpuEntity> queryWrapper = new QueryWrapper<>();
        if(cid != 0){
            queryWrapper.eq("category_id",cid);
        }
        String key = pageParamVo.getKey();
        if(StringUtils.isNotBlank(key)){
            queryWrapper.and(t ->  t.eq("id",key).or().like("name",key));
        }

        IPage<SpuEntity> page = this.page(
                pageParamVo.getPage(),
                queryWrapper
        );

        return new PageResultVo(page);

    }

    @Autowired
    private SpuDescMapper spuDescMapper;

    @Autowired
    private SpuAttrValueService spuAttrValueService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Autowired
    private GmallSmsClient gmallSmsClient;

    @Autowired
    private SpuDescService spuDescService;

    @GlobalTransactional
    @Override
    public void bigSave(SpuVo spuVo) {
        //1.spu相关信息表保存
        //1.1 pms_spu 表
        Long spuId = saveSpu(spuVo);

        //1.2 pms_spu_desc表
        spuDescService.saveSpuDesc(spuVo, spuId);

        //1.3 psm_spu_attr_value表
        saveBaseAttr(spuVo, spuId);

        //2.sku相关信息表保存 pms_sku pms_sku_images pms_sku_attr_value
        saveSku(spuVo, spuId);

//        int i = 1/0;
    }

    private void saveSku(SpuVo spuVo, Long spuId) {
        List<SkuVo> skuVos = spuVo.getSkus();
        if(CollectionUtils.isEmpty(skuVos)){
           return;
        }
        skuVos.forEach(skuVo -> {
            //2.1 pms_sku 表
            skuVo.setId(null);
            skuVo.setSpuId(spuId);
            skuVo.setBrandId(spuVo.getBrandId());
            skuVo.setCatagoryId(spuVo.getCategoryId());
            List<String> images = skuVo.getImages();
            if(!CollectionUtils.isEmpty(images)){
                skuVo.setDefaultImage(skuVo.getDefaultImage() == null?images.get(0):skuVo.getDefaultImage());
            }
            skuMapper.insert(skuVo);
            Long skuId = skuVo.getId();

            //2.2 pms_sku_images表
            if(!CollectionUtils.isEmpty(images)){
               List<SkuImagesEntity> imagesEntities = images.stream().map(image -> {
                   SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                   skuImagesEntity.setId(null);
                   skuImagesEntity.setSort(1);
                   skuImagesEntity.setSkuId(skuId);
                   skuImagesEntity.setUrl(image);
                   skuImagesEntity.setDefaultStatus(0);
                   if(StringUtils.equals(image,skuVo.getDefaultImage())){
                       skuImagesEntity.setDefaultStatus(1);
                   }
                   return skuImagesEntity;
               }).collect(Collectors.toList());
               skuImagesService.saveBatch(imagesEntities);
            }
            //2.3 pms_sku_attr_value表
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            if(!CollectionUtils.isEmpty(saleAttrs)){
                saleAttrs.forEach(attr -> {
                    attr.setId(null);
                    attr.setSkuId(skuId);
                    attr.setSort(0);
                });
                skuAttrValueService.saveBatch(saleAttrs);
            }

            //3.优惠信息表保存
            SkuSalesVo skuSalesVo = new SkuSalesVo();
            BeanUtils.copyProperties(skuVo,skuSalesVo);
            skuSalesVo.setSkuId(skuId);
            gmallSmsClient.saveSkuSales(skuSalesVo);
        });
    }

    private void saveBaseAttr(SpuVo spuVo, Long spuId) {
        List<SpuAttrValueVo> baseAttrs = spuVo.getBaseAttrs();
        if(!CollectionUtils.isEmpty(baseAttrs)){
            List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrs.stream().map(spuAttrValueVo -> {
                SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                BeanUtils.copyProperties(spuAttrValueVo, spuAttrValueEntity);
                spuAttrValueEntity.setAttrId(null);
                spuAttrValueEntity.setSpuId(spuId);
                spuAttrValueEntity.setSort(1);
                return spuAttrValueEntity;
            }).collect(Collectors.toList());

            spuAttrValueService.saveBatch(spuAttrValueEntities);
        }
    }

    private Long saveSpu(SpuVo spuVo) {
        spuVo.setCreateTime(new Date());
        spuVo.setUpdateTime(spuVo.getCreateTime());
        spuVo.setId(null);
        this.save(spuVo);
        return spuVo.getId();
    }

}