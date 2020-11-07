package com.karson.mall.member.dao;

import com.karson.mall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-07 16:08:57
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
