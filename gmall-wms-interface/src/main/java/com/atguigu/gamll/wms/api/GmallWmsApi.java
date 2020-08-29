package com.atguigu.gamll.wms.api;

import com.atguigu.gamll.wms.entity.WareSkuEntity;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface GmallWmsApi {

    @GetMapping("wms/waresku/sku/{skuId}")
    public ResponseVo<List<WareSkuEntity>> queryWareSkusBySkuId(@PathVariable("skuId")Long skuId);

}
