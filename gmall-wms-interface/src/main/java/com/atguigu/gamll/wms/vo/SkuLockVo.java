package com.atguigu.gamll.wms.vo;

import lombok.Data;

@Data
public class SkuLockVo {

    private Long skuId;
    private Integer count;

    private Boolean lock = false;//锁定状态
    private Long wareSkuId; //锁定成功情况下记录锁定成功的仓库Id
}
