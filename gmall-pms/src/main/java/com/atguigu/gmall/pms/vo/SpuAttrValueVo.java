package com.atguigu.gmall.pms.vo;

import com.atguigu.gamll.pms.entity.SpuAttrValueEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class SpuAttrValueVo extends SpuAttrValueEntity {

    public void setValueSelected(List<String> valueSelected) {
        if(CollectionUtils.isEmpty(valueSelected)){
            return;
        }
        this.setAttrValue(StringUtils.join(valueSelected,","));
    }
}
