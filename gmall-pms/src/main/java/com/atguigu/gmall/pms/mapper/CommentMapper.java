package com.atguigu.gmall.pms.mapper;

import com.atguigu.gamll.pms.entity.CommentEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价
 * 
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-08-21 17:46:22
 */
@Mapper
public interface CommentMapper extends BaseMapper<CommentEntity> {
	
}
