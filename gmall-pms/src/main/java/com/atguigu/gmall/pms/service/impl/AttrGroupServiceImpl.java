package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.service.AttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrMapper attrMapper;

    @Override
    public List<AttrGroupEntity> queryGroupWithAttrsByCatId(Long catId) {
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id",catId);
        List<AttrGroupEntity> groups = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(groups)){
            return null;
        }
        //使用AttrService类
       /* for (AttrGroupEntity group : groups) {
            QueryWrapper<AttrEntity> attrEntity = new QueryWrapper<>();
            attrEntity.eq("group_id",group.getId());
            attrEntity.eq("type",1);
            List<AttrEntity> attrEntities = attrService.list(attrEntity);
            group.setAttrEntities(attrEntities);
        }*/

        //用AttrMapper类
        groups.forEach(group -> {
            QueryWrapper<AttrEntity> attrEntity = new QueryWrapper<>();
            attrEntity.eq("group_id",group.getId());
            attrEntity.eq("type",1);
            List<AttrEntity> attrEntities = attrMapper.selectList(attrEntity);
            group.setAttrEntities(attrEntities);
        });
        return groups;

    }

}