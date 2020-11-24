package com.karson.mall.member.dao;

import com.karson.mall.member.entity.MemberLevelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员等级
 * 
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-07 16:08:56
 */
@Mapper
public interface MemberLevelDao extends BaseMapper<MemberLevelEntity> {

    MemberLevelEntity getDefaultLevel();
}
