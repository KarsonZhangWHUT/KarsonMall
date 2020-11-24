package com.karson.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.karson.common.utils.PageUtils;
import com.karson.common.utils.Query;
import com.karson.mall.product.dao.CategoryDao;
import com.karson.mall.product.entity.CategoryEntity;
import com.karson.mall.product.service.CategoryBrandRelationService;
import com.karson.mall.product.service.CategoryService;
import com.karson.mall.product.vo.Catalog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {


    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1.查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2.组装成父子树形结构
        //2.1找到所有的一级分类
        List<CategoryEntity> level1Menu = entities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0)
                .map((menu) -> {
                    menu.setChildren(getChildren(menu, entities));
                    return menu;
                }).sorted((menu1, menu2) -> {
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                }).collect(Collectors.toList());

        return level1Menu;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //先检查
        //TODO 检查当前删除的菜单，是否被别的地方引用
        //逻辑删除
        //1.配置全局的逻辑删除规则
        //2.配置逻辑删除的组件（高版本省略）
        //3.给Bean加上逻辑删除注解@TableLogic
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(paths, catelogId);

        Collections.reverse(parentPath);

        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     *
     * @CacheEvict:失效模式
     */

//    @Caching(evict = {
//            @CacheEvict(value = {"category"}, key = "'getLevel1Category'"),
//            @CacheEvict(value = {"category"}, key = "'getCatalogJson'")
//    })
    @CacheEvict(value = "category", allEntries = true)//删除分许下的所有缓存
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        //同时修改缓存中的数据或者删掉缓存等待下一次查询操作更新缓存
    }

    @Override
    @Cacheable(value = "category", key = "#root.methodName") //代表当前结果需要缓存，如果缓存中有，方法不需要调用。如果缓存中没有，会调用方法将方法返回值缓存起来
    public List<CategoryEntity> getLevel1Category() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    @Cacheable(value = "category", key = "#root.methodName")
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        //将数据库的多次查询变为1次
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);
        //查出所有1级分类
        List<CategoryEntity> level1Category = getParentCid(selectList, 0L);
        //封装数据
        Map<String, List<Catalog2Vo>> parentCid = level1Category.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //每个的一级分类，查到这个一级分类的所有二级分类
            List<CategoryEntity> level1Categories = getParentCid(selectList, v.getCatId());
            //封装上面的结果
            List<Catalog2Vo> catalog2Vos = null;
            if (level1Categories != null) {
                catalog2Vos = level1Categories.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //找当前二级分类的三级分类，封装成vo
                    List<CategoryEntity> level3Catalog = getParentCid(selectList, l2.getCatId());
                    if (level3Catalog != null) {
                        List<Catalog2Vo.Catalog3Vo> catalog3Vos = level3Catalog.stream().map(l3 -> {
                            //封装成指定格式
                            return new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(catalog3Vos);
                    }
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));
        return parentCid;
    }

    //TODO 产生堆外内存溢出：OutOfDirectMemoryError
//    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson2() {
        //1.加入缓存逻辑
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {
            //没有缓存，查询数据库
            Map<String, List<Catalog2Vo>> catalogJsonFromDB = getCatalogJsonFromDBWithLocalLock();
            //查到的数据再放入缓存,将查出的对象翻入缓存中(以后缓存中n字符串，因为json跨语言，跨平台兼容)
            String s = JSON.toJSONString(catalogJsonFromDB);
            stringRedisTemplate.opsForValue().set("catalogJson", s, 1, TimeUnit.DAYS);
            return catalogJsonFromDB;
        }
        //json字符串反序列化为对象
        return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
        });
    }

    /**
     * 缓存里面的数据如何和数据库保持一致
     * 双写模式(要加锁避免并发问题)，失效模式
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithRedissonLock() {

        //锁的名字，名字一样，锁就一样；锁的粒度(锁的粒度越细，就越快)
        RLock lock = redissonClient.getLock("catalogJson-lock");
        lock.lock();
        Map<String, List<Catalog2Vo>> dataFromDB;
        try {
            dataFromDB = getDataFromDB();
        } finally {
            lock.unlock();
        }
        return dataFromDB;
    }


    private Map<String, List<Catalog2Vo>> getDataFromDB() {
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            //缓存不为null直接返回
            return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
        }

        //将数据库的多次查询变为1次
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);

        //查出所有1级分类
//            List<CategoryEntity> level1Category = getLevel1Category();
        List<CategoryEntity> level1Category = getParentCid(selectList, 0L);

        //封装数据
        Map<String, List<Catalog2Vo>> parentCid = level1Category.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //每个的一级分类，查到这个一级分类的所有二级分类
            List<CategoryEntity> level1Categories = getParentCid(selectList, v.getCatId());
            //封装上面的结果
            List<Catalog2Vo> catalog2Vos = null;
            if (level1Categories != null) {
                catalog2Vos = level1Categories.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //找当前二级分类的三级分类，封装成vo
                    List<CategoryEntity> level3Catalog = getParentCid(selectList, l2.getCatId());
                    if (level3Catalog != null) {
                        List<Catalog2Vo.Catalog3Vo> catalog3Vos = level3Catalog.stream().map(l3 -> {
                            //封装成指定格式
                            return new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(catalog3Vos);
                    }
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));
        //查到数据放入缓存
        String s = JSON.toJSONString(parentCid);
        stringRedisTemplate.opsForValue().set("catalogJson", s, 1, TimeUnit.DAYS);
        return parentCid;
    }


    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithLocalLock() {


        //本地锁，只锁当前进程，在分布式情况下，想要锁住所有，必须使用分布式锁
        synchronized (this) {
            //得到锁以后，再去缓存中查询一次
            String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
            if (!StringUtils.isEmpty(catalogJson)) {
                return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
                });
            }

            System.out.println("查询了数据库");

            //将数据库的多次查询变为1次
            List<CategoryEntity> selectList = this.baseMapper.selectList(null);

            //查出所有1级分类
//            List<CategoryEntity> level1Category = getLevel1Category();
            List<CategoryEntity> level1Category = getParentCid(selectList, 0L);

            //封装数据
            Map<String, List<Catalog2Vo>> parentCid = level1Category.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                //每个的一级分类，查到这个一级分类的所有二级分类
                List<CategoryEntity> level1Categories = getParentCid(selectList, v.getCatId());
                //封装上面的结果
                List<Catalog2Vo> catalog2Vos = null;
                if (level1Categories != null) {
                    catalog2Vos = level1Categories.stream().map(l2 -> {
                        Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                        //找当前二级分类的三级分类，封装成vo
                        List<CategoryEntity> level3Catalog = getParentCid(selectList, l2.getCatId());
                        if (level3Catalog != null) {
                            List<Catalog2Vo.Catalog3Vo> catalog3Vos = level3Catalog.stream().map(l3 -> {
                                //封装成指定格式
                                return new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            }).collect(Collectors.toList());
                            catalog2Vo.setCatalog3List(catalog3Vos);
                        }
                        return catalog2Vo;
                    }).collect(Collectors.toList());
                }
                return catalog2Vos;
            }));
            String s = JSON.toJSONString(parentCid);
            stringRedisTemplate.opsForValue().set("catalogJson", s, 1, TimeUnit.DAYS);
            return parentCid;
        }
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> entities, Long pId) {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", pId));
    }

    private List<Long> findParentPath(List<Long> paths, Long catelogId) {
        //1.收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(paths, byId.getParentCid());
        }
        return paths;
    }

    /**
     * 获取当前分类的子分类
     * 递归查找所有菜单的子菜单
     */
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().equals(root.getCatId());
        }).map(categoryEntity -> {
            //找到子菜单，找子菜单的子菜单
            categoryEntity.setChildren(getChildren(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            //菜单的排序
//            return menu1.getSort() - menu2.getSort();
//            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return children;
    }

}