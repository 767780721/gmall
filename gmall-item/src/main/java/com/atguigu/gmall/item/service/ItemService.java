package com.atguigu.gmall.item.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gamll.pms.entity.*;
import com.atguigu.gamll.pms.vo.GroupVo;
import com.atguigu.gamll.pms.vo.SaleAttrValueVo;
import com.atguigu.gamll.wms.entity.WareSkuEntity;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.ItemException;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class ItemService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;


    public ItemVo loadData(Long skuId) {
        ItemVo itemVo = new ItemVo();

        CompletableFuture<SkuEntity> skuCompletableFuture = CompletableFuture.supplyAsync(() -> {
//        1.根据skuId查询sku信息 Y
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                throw new ItemException("该skuId对应的商品不存在");
            }
            itemVo.setSkuId(skuEntity.getId());
            itemVo.setTitle(skuEntity.getTitle());
            itemVo.setSubTitle(skuEntity.getSubtitle());
            itemVo.setPrice(skuEntity.getPrice());
            itemVo.setWeight(skuEntity.getWeight());
            itemVo.setDefaultImage(skuEntity.getDefaultImage());
            return skuEntity;
        }, threadPoolExecutor);


        //        2.根据cid3查询一二三级分类集合 Y
        CompletableFuture<Void> cateCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<CategoryEntity>> allCatogoriesVo = pmsClient.queryAllCategoriesByCid3(skuEntity.getCatagoryId());
            List<CategoryEntity> categoryEntities = allCatogoriesVo.getData();
            itemVo.setCategories(categoryEntities);
        }, threadPoolExecutor);

//        3.根据品牌id查询品牌信息 Y
        CompletableFuture<Void> brandCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<BrandEntity> brandEntityResponseVo = pmsClient.queryBrandById(skuEntity.getBrandId());
            BrandEntity brandEntity = brandEntityResponseVo.getData();
            if (brandEntity != null) {
                itemVo.setBrandId(brandEntity.getId());
                itemVo.setBrandName(brandEntity.getName());
            }
        }, threadPoolExecutor);

//        4.根据spuId查询spu信息 Y
        CompletableFuture<Void> spuCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<SpuEntity> spuEntityResponseVo = pmsClient.querySpuById(skuEntity.getSpuId());
            SpuEntity spuEntity = spuEntityResponseVo.getData();
            if (spuEntity != null) {
                itemVo.setSpuId(spuEntity.getId());
                itemVo.setSpuName(spuEntity.getName());
            }
        }, threadPoolExecutor);

//        5.根据skuId查询优惠信息（sms） Y
        CompletableFuture<Void> salesCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<ItemSaleVo>> salesVo = smsClient.querySaleVoBySkuId(skuId);
            List<ItemSaleVo> itemSaleVos = salesVo.getData();
            itemVo.setSales(itemSaleVos);
        }, threadPoolExecutor);

//        6.根据skuId查询库存信息 Y
        CompletableFuture<Void> wareCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<WareSkuEntity>> wareVo = wmsClient.queryWareSkusBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                boolean store = wareSkuEntities.stream().allMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0);
                itemVo.setStore(store);
            }
        }, threadPoolExecutor);

//        7.根据skuId查询sku的图片列表 Y
        CompletableFuture<Void> imageCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuImagesEntity>> skuImagesVo = pmsClient.queryImagesBuSkuId(skuId);
            List<SkuImagesEntity> skuImagesEntities = skuImagesVo.getData();
            if (!CollectionUtils.isEmpty(skuImagesEntities)) {
                itemVo.setImages(skuImagesEntities);
            }
        }, threadPoolExecutor);

//        8.根据spuId查询spu下所有sku的销售属性组合 Y
        CompletableFuture<Void> saleAttrsCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<SaleAttrValueVo>> saleAttrsVo = pmsClient.querySaleAttrVoBySpuId(skuEntity.getSpuId());
            List<SaleAttrValueVo> saleAttrValueVos = saleAttrsVo.getData();
            itemVo.setSaleAttrs(saleAttrValueVos);
        }, threadPoolExecutor);

//        9.根据skuId查询当前sku的销售属性  Y
        CompletableFuture<Void> saleAttrCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuAttrValueEntity>> saleAttrValuesResponseVo = pmsClient.querySaleAttrValuesBySkuId(skuId);
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrValuesResponseVo.getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                Map<Long, String> saleAttr = skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue));
                itemVo.setSaleAttr(saleAttr);
            }
        }, threadPoolExecutor);

//        10.根据spuId查询spu下所有销售属性组合和skuId的映射关系 Y
        CompletableFuture<Void> mappingCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<String> mappingResponseVo = pmsClient.querySaleAttrValuesMappingSkuIdBySpuId(skuEntity.getSpuId());
            String skuJsons = mappingResponseVo.getData();
            itemVo.setSkuJsons(skuJsons);
        }, threadPoolExecutor);

//        11.根据spuId查询spu的海报信息列表 Y
        CompletableFuture<Void> descCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<SpuDescEntity> spuDescEntityResponseVo = pmsClient.querySpuDescById(skuEntity.getSpuId());
            SpuDescEntity spuDescEntity = spuDescEntityResponseVo.getData();
            if (spuDescEntity != null && StringUtils.isNotBlank(spuDescEntity.getDecript())) {
                String decript = spuDescEntity.getDecript();
                String[] urls = StringUtils.split(decript, ",");
                itemVo.setSpuImages(Arrays.asList(urls));
            }
        }, threadPoolExecutor);

//        12.根据cid3、spuId、skuId查询分组及组下的规格参数以及值
        CompletableFuture<Void> groupCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<GroupVo>> groupResponseVo = pmsClient.queryGroupVoByCidAndSpuIdAndSkuId(skuEntity.getCatagoryId(), skuEntity.getSpuId(), skuId);
            List<GroupVo> groupVos = groupResponseVo.getData();
            itemVo.setGroups(groupVos);
        }, threadPoolExecutor);

        CompletableFuture.allOf(cateCompletableFuture,brandCompletableFuture,spuCompletableFuture,
                salesCompletableFuture,wareCompletableFuture,imageCompletableFuture,saleAttrsCompletableFuture,
                saleAttrCompletableFuture,mappingCompletableFuture,descCompletableFuture,groupCompletableFuture).join();

        return itemVo;
    }
}
