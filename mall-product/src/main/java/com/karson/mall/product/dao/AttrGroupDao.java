package com.karson.mall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.karson.mall.product.entity.AttrGroupEntity;
import com.karson.mall.product.vo.SpuItemAttrGroupVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 * 
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-06 21:40:52
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(@Param("spuId") Long spuId,
                                                          @Param("catalogId") Long catalogId);

}
