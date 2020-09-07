package com.atguigu.gmall.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.ums.entity.UserCollectSubjectEntity;

import java.util.Map;

/**
 * 关注活动表
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-09-06 19:57:48
 */
public interface UserCollectSubjectService extends IService<UserCollectSubjectEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

