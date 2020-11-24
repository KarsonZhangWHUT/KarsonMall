package com.karson.mall.product;

import com.karson.mall.product.dao.AttrGroupDao;
import com.karson.mall.product.entity.BrandEntity;
import com.karson.mall.product.service.BrandService;
import com.karson.mall.product.service.CategoryService;
import com.karson.mall.product.vo.SpuItemAttrGroupVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@Slf4j
class MallProductApplicationTests {

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Resource
    AttrGroupDao attrGroupDao;

    @Test
    public void testDemo() {
        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId =
                attrGroupDao.getAttrGroupWithAttrsBySpuId(3L, 225L);
        System.out.println(attrGroupWithAttrsBySpuId);

    }

    @Test
    public void demo2() {
        System.out.println(redissonClient);
    }

    @Test
    public void demo() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("hello", "world" + UUID.randomUUID().toString());

        System.out.println(ops.get("hello"));
    }


    @Test
    public void testFindPath() {
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路径{}", Arrays.asList(catelogPath));
    }


//    @Autowired
//    private OSSClient ossClient;
//
//    @Test
//    public void testUpload2() throws FileNotFoundException {
//        ossClient.putObject("karson-mall", "L181kk2Eou-compress.jpg", new FileInputStream("E:\\images\\L181kk2Eou-compress.jpg"));
//    }
//
//    @Test
//    public void testUpload() throws FileNotFoundException {
//        // Endpoint以杭州为例，其它Region请按实际情况填写。
//        String endpoint = "oss-cn-hangzhou.aliyuncs.com";
//        // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
//        String accessKeyId = "LTAI4G5UdxxJjw8KVwY1RA1j";
//        String accessKeySecret = "s9ylZuxMUdANKE3zsuCvxTKCuTsRzY";
//
//        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//
//        // 上传文件流。
//        InputStream inputStream = new FileInputStream("E:\\images\\Kernel_Layout.png");
//        ossClient.putObject("karson-mall", "Kernel_Layout.png", inputStream);
//
//        // 关闭OSSClient。
//        ossClient.shutdown();
//        System.out.println("上传完成");
//    }


    @Autowired
    BrandService brandService;

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("华为");
        brandService.save(brandEntity);
        System.out.println("保存成功");
    }

}
