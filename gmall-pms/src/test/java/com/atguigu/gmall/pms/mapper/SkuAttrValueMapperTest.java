package com.atguigu.gmall.pms.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
class SkuAttrValueMapperTest {

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;


    @Test
    void querySaleAttrValuesMappingSkuIdBySpuId() {
        List<Map<String, Object>> stringLongMap = skuAttrValueMapper.querySaleAttrValuesMappingSkuIdBySpuId(7l);
        System.out.println(stringLongMap);

    }
}