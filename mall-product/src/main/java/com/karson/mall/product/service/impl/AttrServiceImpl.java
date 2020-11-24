package com.karson.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.karson.common.constant.ProductConstant;
import com.karson.common.utils.PageUtils;
import com.karson.common.utils.Query;
import com.karson.mall.product.dao.AttrAttrgroupRelationDao;
import com.karson.mall.product.dao.AttrDao;
import com.karson.mall.product.dao.AttrGroupDao;
import com.karson.mall.product.dao.CategoryDao;
import com.karson.mall.product.entity.AttrAttrgroupRelationEntity;
import com.karson.mall.product.entity.AttrEntity;
import com.karson.mall.product.entity.AttrGroupEntity;
import com.karson.mall.product.entity.CategoryEntity;
import com.karson.mall.product.service.AttrService;
import com.karson.mall.product.service.CategoryService;
import com.karson.mall.product.vo.AttrGroupRelationVo;
import com.karson.mall.product.vo.AttrResponseVo;
import com.karson.mall.product.vo.AttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Resource
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Resource
    private AttrGroupDao attrGroupDao;

    @Resource
    private CategoryDao categoryDao;

    @Autowired
    private CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);//要求两个类的字段名相同
        //1.保存基本数据
        this.save(attrEntity);
        //2.保存关联关系,只有规格参数才和分组属性有关联关系
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationDao.insert(relationEntity);
        }
    }

    @Transactional
    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType) {

        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("attr_type", "base".equalsIgnoreCase(attrType) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrResponseVo> responseVos = records.stream().map((attrEntity -> {
            AttrResponseVo attrResponseVo = new AttrResponseVo();
            BeanUtils.copyProperties(attrEntity, attrResponseVo);
            //设置分类和分组的名字

            AttrAttrgroupRelationEntity attrId = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
            if (attrId != null && attrId.getAttrGroupId() != null) {
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrId.getAttrGroupId());
                if (attrGroupEntity != null)
                    attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrResponseVo.setCatelogName(categoryEntity.getName());
            }
            return attrResponseVo;
        })).collect(Collectors.toList());

        pageUtils.setList(responseVos);
        return pageUtils;
    }

    @Cacheable(value = "attr", key = "'attrInfo:'+#root.args[0]")
    @Transactional
    @Override
    public AttrResponseVo getAttrInfo(Long attrId) {
        AttrResponseVo attrResponseVo = new AttrResponseVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrResponseVo);


        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            //1.设置分组信息
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (attrAttrgroupRelationEntity != null) {
                attrResponseVo.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                if (attrGroupEntity != null) {
                    attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }

        //2.设置分类信息
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        attrResponseVo.setCatelogPath(catelogPath);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        if (categoryEntity != null)
            attrResponseVo.setCatelogName(categoryEntity.getName());

        System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"+attrResponseVo);
        return attrResponseVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            //修改分组关联
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attr.getAttrId());
//        attrAttrgroupRelationDao.update(relationEntity,
//                new UpdateWrapper<AttrAttrgroupRelationEntity>()
//                        .eq("attr_id", attr.getAttrId()));
            Integer attrCount = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>()
                    .eq("attr_id", attr.getAttrId()));
            if (attrCount > 0) {
                //修改分组关联
//            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
//            relationEntity.setAttrGroupId(attr.getAttrGroupId());
//            relationEntity.setAttrId(attr.getAttrId());
                attrAttrgroupRelationDao.update(relationEntity,
                        new UpdateWrapper<AttrAttrgroupRelationEntity>()
                                .eq("attr_id", attr.getAttrId()));
            } else {
                //新增操作
                attrAttrgroupRelationDao.insert(relationEntity);
            }
        }
    }

    /**
     * 根据分组id查找关联的所有属性
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrGroupId) {
        List<AttrAttrgroupRelationEntity> list = attrAttrgroupRelationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>()
                        .eq("attr_group_id", attrGroupId));
        List<Long> collect = list.stream().map((AttrAttrgroupRelationEntity::getAttrId)).collect(Collectors.toList());
        if (collect.size() == 0) {
            return null;
        }
        return this.listByIds(collect);
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        //这种情况，因为是一个集合，要发很多次请求
        //attrAttrgroupRelationDao.delete(new QueryWrapper<>().eq("attr_id", ).eq("attr_group_id", ));
        //发一次请求，进行删除操作
        //delete from `relation` where (`attr_id=? and attr_group_id=?`) or (`attr_id=? and attr_group_id=?`)
        List<AttrAttrgroupRelationEntity> collect = Arrays.asList(vos).stream().map((vo) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(vo, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        attrAttrgroupRelationDao.deleteBatchRelation(collect);
    }

    /**
     * 获取当前分组没有关联的所有属性
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrGroupId) {
        //1.当前分组只能关联自己所属的分类的里面的属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //2.当前分组只能关联别的分组没有引用的属性
        //找到当前分类下的其他分组  这些分组关联的属性  从当前分类的所有属性中移除这些属性
        //找到当前分类下的其他分组
        List<AttrGroupEntity> groupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>()
                .eq("catelog_id", catelogId));
        List<Long> collect = groupEntities.stream().map(
                AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());

        //这些分组关联的属性
        List<AttrAttrgroupRelationEntity> groupId = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>()
                .in("attr_group_id", collect));
        List<Long> attrIds = groupId.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        //从当前分类的所有属性中移除这些属性
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>()
                .eq("catelog_id", catelogId).eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (attrIds.size() != 0) {
            wrapper.notIn("attr_id", attrIds);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);

        return new PageUtils(page);

    }

    @Override
    public List<Long> selectSearchAttrsIds(List<Long> list) {
        return this.baseMapper.selectSearchAttrsIds(list);
    }

}