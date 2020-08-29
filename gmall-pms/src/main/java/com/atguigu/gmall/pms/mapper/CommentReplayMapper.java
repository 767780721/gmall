package com.atguigu.gmall.pms.mapper;

import com.atguigu.gamll.pms.entity.CommentReplayEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价回复关系
 * 
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-08-21 17:46:22
 */
@Mapper
public interface CommentReplayMapper extends BaseMapper<CommentReplayEntity> {
	
}
