package com.karson.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.karson.common.utils.PageUtils;
import com.karson.mall.product.entity.AttrEntity;
import com.karson.mall.product.vo.AttrGroupRelationVo;
import com.karson.mall.product.vo.AttrResponseVo;
import com.karson.mall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-06 21:40:52
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType);

    AttrResponseVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrGroupId);

    void deleteRelation(AttrGroupRelationVo[] vos);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrGroupId);

    /**
     * 在指定的所有属性集合里面，选出检索属性
     */
    List<Long> selectSearchAttrsIds(List<Long> list);
}

