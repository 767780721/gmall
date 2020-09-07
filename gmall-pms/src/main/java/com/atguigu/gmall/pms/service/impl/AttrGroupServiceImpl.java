package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gamll.pms.entity.AttrEntity;
import com.atguigu.gamll.pms.entity.AttrGroupEntity;
import com.atguigu.gamll.pms.entity.SkuAttrValueEntity;
import com.atguigu.gamll.pms.entity.SpuAttrValueEntity;
import com.atguigu.gamll.pms.vo.AttrValueVo;
import com.atguigu.gamll.pms.vo.GroupVo;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.service.AttrService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
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
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SpuAttrValueMapper spuAttrValueMapper;

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

    @Override
    public List<GroupVo> queryGroupVoByCidAndSpuIdAndSkuId(Long cid, Long spuId, Long skuId) {
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id",cid));
        if(CollectionUtils.isEmpty(attrGroupEntities)){
            return null;
        }

        return attrGroupEntities.stream().map(attrGroupEntity -> {
            GroupVo groupVo = new GroupVo();
            groupVo.setGroupId(attrGroupEntity.getId());
            groupVo.setName(attrGroupEntity.getName());
            //获取规格参数分组下的 所有规格参数
            List<AttrEntity> attrEntities = attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", attrGroupEntity.getId()));
            if(!CollectionUtils.isEmpty(attrEntities)){
                List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());
                List<AttrValueVo> attrValueVos = new ArrayList<>();
                //结合skuId查询销售属性值
                List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().in("attr_id", attrIds).eq("sku_id", skuId));
                if(!CollectionUtils.isEmpty(skuAttrValueEntities)){
                    List<AttrValueVo> skuAttrValueVos = skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                        AttrValueVo attrValueVo = new AttrValueVo();
                        BeanUtils.copyProperties(skuAttrValueEntity,attrValueVo);
                        return attrValueVo;
                    }).collect(Collectors.toList());
                    attrValueVos.addAll(skuAttrValueVos);
                }
                //结合spuId 查询通用属性和值
                List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrValueMapper.selectList(new QueryWrapper<SpuAttrValueEntity>().in("attr_id", attrIds).eq("spu_id", spuId));
                if(!CollectionUtils.isEmpty(spuAttrValueEntities)){
                    List<AttrValueVo> spuAttrValueVos = spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                        AttrValueVo attrValueVo = new AttrValueVo();
                        BeanUtils.copyProperties(spuAttrValueEntity, attrValueVo);
                        return attrValueVo;
                    }).collect(Collectors.toList());
                    attrValueVos.addAll(spuAttrValueVos);
                }
                groupVo.setAttrs(attrValueVos);
            }
            return groupVo;
        }).collect(Collectors.toList());
    }

}