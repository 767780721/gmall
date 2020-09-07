package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gamll.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.service.CategoryService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<CategoryEntity> queryCategoriesWithSubByPid(Long pid) {
        return categoryMapper.queryCategoriesWithSubByPid(pid);
    }

    @Override
    public List<CategoryEntity> queryAllCategoriesByCid3(Long cid) {
        //查询三级分类
        CategoryEntity levelThreeCat = this.getById(cid);
        if(levelThreeCat != null){
            //查询二级分类
            CategoryEntity levelTwoCat = this.getById(levelThreeCat.getParentId());
            //查询一级分类
            CategoryEntity levelOneCat = this.getById(levelTwoCat.getParentId());

            return Arrays.asList(levelOneCat,levelTwoCat,levelThreeCat);
        }
        return null;
    }

}