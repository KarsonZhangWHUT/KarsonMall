package com.karson.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.karson.common.utils.PageUtils;
import com.karson.common.utils.Query;
import com.karson.mall.product.dao.AttrGroupDao;
import com.karson.mall.product.entity.AttrEntity;
import com.karson.mall.product.entity.AttrGroupEntity;
import com.karson.mall.product.service.AttrGroupService;
import com.karson.mall.product.service.AttrService;
import com.karson.mall.product.vo.AttrGroupWithAttrsVo;
import com.karson.mall.product.vo.SpuItemAttrGroupVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        //select * from pms_attr_group where catelog_id=? and (attr_group_id or attr_group_name like %key%)
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) ->
                    obj.eq("attr_group_id", key).or().like("attr_group_name", key));
        }
        if (catelogId == 0) {
            //查询所有
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    wrapper);
            return new PageUtils(page);
        } else {
//            String key = (String) params.get("key");
//            //select * from pms_attr_group where catelog_id=? and (attr_group_id or attr_group_name like %key%)
//            QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId);
//            if (!StringUtils.isEmpty(key)) {
//                wrapper.and((obj) ->
//                        obj.eq("attr_group_id", key).or().like("attr_group_name", key));
//            }
            wrapper.eq("catelog_id", catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    wrapper);
            return new PageUtils(page);
        }
    }

    /**
     * 根据分类ID查出所有的分组以及这些组里面的属性
     */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        //1.查询分组信息
        List<AttrGroupEntity> entities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        //2.查询所哟属性
        List<AttrGroupWithAttrsVo> collect = entities.stream().map(item -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item, attrGroupWithAttrsVo);
            List<AttrEntity> attrs = attrService.getRelationAttr(attrGroupWithAttrsVo.getAttrGroupId());
            attrGroupWithAttrsVo.setAttrs(attrs);
            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        //查出当前spu对应的所有属性的分组信息和当前分组下的所有属性对应的值
        AttrGroupDao attrGroupDao = this.baseMapper;
        List<SpuItemAttrGroupVo> vos =attrGroupDao.getAttrGroupWithAttrsBySpuId(spuId,catalogId);
        return vos;
    }

}
