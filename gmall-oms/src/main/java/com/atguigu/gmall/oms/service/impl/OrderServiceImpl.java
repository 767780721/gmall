package com.atguigu.gmall.oms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gamll.pms.entity.*;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atuigu.gmall.oms.entity.OrderItemEntity;
import com.atguigu.gmall.oms.feign.GmallPmsClient;
import com.atguigu.gmall.oms.feign.GmallUmsClient;
import com.atguigu.gmall.oms.mapper.OrderItemMapper;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atuigu.gmall.oms.vo.OrderItemVo;
import com.atuigu.gmall.oms.vo.OrderSubmitVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.atuigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.service.OrderService;
import org.springframework.transaction.annotation.Transactional;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> implements OrderService {

    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private OrderItemMapper itemMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<OrderEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<OrderEntity>()
        );

        return new PageResultVo(page);
    }

    @Transactional
    @Override
    public OrderEntity addOrder(OrderSubmitVo submitVo,Long userId) {
        //1. 新增订单
        OrderEntity orderEntity = new OrderEntity();
        //用户信息
        orderEntity.setUserId(userId);
        ResponseVo<UserEntity> userEntityResponseVo = umsClient.queryUserById(userId);
        UserEntity userEntity = userEntityResponseVo.getData();
        orderEntity.setUsername(userEntity.getUsername());

        //订单编号
        orderEntity.setOrderSn(submitVo.getOrderToken());

        //创建时间
        orderEntity.setCreateTime(new Date());

        //订单总额
        orderEntity.setTotalAmount(submitVo.getTotalPrice());

        //应付总额
        orderEntity.setPayAmount(submitVo.getTotalPrice());

        //运费金额
        orderEntity.setPromotionAmount(new BigDecimal(10));

        //支付方式
        orderEntity.setPayType(submitVo.getPayType());

        //订单来源
        orderEntity.setSourceType(1);

        //订单状态
        orderEntity.setStatus(0);

        //物流公司
        orderEntity.setDeliveryCompany(submitVo.getDeliveryCompany());

        UserAddressEntity address = submitVo.getAddress();
        //收件人
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverAddress(address.getAddress());
        orderEntity.setReceiverCity(address.getCity());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverRegion(address.getRegion());
        orderEntity.setReceiverPostCode(address.getPostCode());
        orderEntity.setReceiverProvince(address.getProvince());

        //收货状态
        orderEntity.setConfirmStatus(0);
        //删除状态
        orderEntity.setDeleteStatus(0);
        //下单时使用的积分
        orderEntity.setUseIntegration(submitVo.getBounds());

        this.save(orderEntity);
        Long orderEntityId = orderEntity.getId();

        //2. 新增订单详情
        List<OrderItemVo> items = submitVo.getItems();
        items.forEach(item -> {
            OrderItemEntity itemEntity = new OrderItemEntity();
            itemEntity.setOrderId(orderEntityId);
            itemEntity.setOrderSn(submitVo.getOrderToken());

            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(item.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if(skuEntity != null){
                itemEntity.setSkuId(skuEntity.getId());
                itemEntity.setSkuName(skuEntity.getName());
                itemEntity.setSkuPic(skuEntity.getDefaultImage());
                itemEntity.setSkuPrice(skuEntity.getPrice());
                itemEntity.setSkuQuantity(item.getCount().intValue());
                itemEntity.setCategoryId(skuEntity.getCatagoryId());

            }

            ResponseVo<BrandEntity> brandEntityResponseVo = pmsClient.queryBrandById(skuEntity.getBrandId());
            BrandEntity brandEntity = brandEntityResponseVo.getData();
            if(brandEntity != null){
                itemEntity.setSpuBrand(brandEntity.getName());
            }

            ResponseVo<List<SkuAttrValueEntity>> skuAttrValuesResponseVo = pmsClient.querySaleAttrValuesBySkuId(item.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValuesResponseVo.getData();
            itemEntity.setSkuAttrsVals(JSON.toJSONString(skuAttrValueEntities));

            ResponseVo<SpuEntity> spuEntityResponseVo = pmsClient.querySpuById(skuEntity.getSpuId());
            SpuEntity spuEntity = spuEntityResponseVo.getData();
            if(spuEntity != null){
                itemEntity.setSpuId(skuEntity.getSpuId());
                itemEntity.setSpuName(spuEntity.getName());
            }

            ResponseVo<SpuDescEntity> spuDescEntityResponseVo = pmsClient.querySpuDescById(skuEntity.getSpuId());
            SpuDescEntity spuDescEntity = spuDescEntityResponseVo.getData();
            if(spuDescEntity != null){
                itemEntity.setSpuPic(spuDescEntity.getDecript());
            }

            itemMapper.insert(itemEntity);
        });

       /* try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
*/
//        int i = 1/0;
        return orderEntity;
    }

}