package com.atguigu.gmall.index.service;

import com.atguigu.gamll.pms.entity.CategoryEntity;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndexService {

    @Autowired
    private GmallPmsClient pmsClient;

    public List<CategoryEntity> queryLevelOneCategories() {
        ResponseVo<List<CategoryEntity>> listResponseVo = pmsClient.queryCategoriesByPid(0L);
        return listResponseVo.getData();
    }
}
