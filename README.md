# KarsonMall

## 开发环境

### **选型 ：**

​			**框架：** SpringBoot、SpringCloud、Mybatis Plus、VUE、

​			**技术：** Redis、MySQL、ElasticSearch、RabbitMQ、Nginx、OSS

​			**工具：** Docker、Maven、npm、Git、Linux

​			**第三方服务：** OSS服务、短信验证码

### **SpringCloud组件选择：**

​			**网关：** SpringCloud Gateway

​			**注册配置中心：** Nacos

​			**远程调用：** Open Feign

​			**负载均衡：** Ribbon

​			**分布式Session：** Spring Session

​			**分布式事务：** Seata

​			**限流降级熔断：** Sentinel

## 12个微服务

### 后端

- 认证服务  mall-auth-server
     - 注册：手机号注册（短信验证码）、第三方微博注册，调用mall-member服务
     - 登录：手机号登录、第三方登录，调用mall-member服务
     - 远程调用mall-member  和  mall-third-party
- 商品服务 mall-product
     - 网站首页、产品详情页
     - 产品上架
- 优惠服务 mall-coupon
     - 秒杀商品的上架
- 订单服务 mall-order
     - 提交订单->获取商品信息->锁库存->运费计算->生成订单->支付订单/取消订单/超时未支付
     - 使用MQ延时队列解锁未支付订单锁定的库存，实现数据的最终一致
     - 支付宝支付、
- 用户服务 mall-member
     - 用户管理
     - 获取用户信息，被远程服务调用
- 秒杀服务 mall-seckill
     - 定时任务把三天内的秒杀商品提前放到Redis
     - 分布式信号量来扣减秒杀库存
     - 验证请求的合法性，是否在秒杀时间内、校验随机码是否正确、秒杀场次和商品id是否正确、验证限购数量、这个人是否重复购买
     - 成功获取到信号量，秒杀成功，生成订单号返回给用户，把订单号封装到OrderTO中放到消息队列，mall-order服务的listener去消费消息
- 检索服务 mall-search
     - 根据分类/关键字检索商品
     - 使用elastic search作为全文检索服务器
- 购物车服务 mall-cart
     - 在线购物车、离线购物车
     - Redis存储数据
     - 远程调用mall-product
- 仓储服务 mall-ware
     - 获取商品的库存信息
- 第三方服务 mall-third-party
     - OSS服务
     - SMS服务
- 网关 mall-gateway
     - SpringCloud Gateway
          - 路由
          - 过滤
- 后台管理系统 

### 前端

- 后台管理系统
     - VUE
- 用户界面
     - thymeleaf模板引擎
     - nginx实现动静分离



## 问题的解决方案

密码保存密文：MD加盐

分布式下session不能共享：Spring Session，把服务器端session数据放到Redis中

未登录请求拦截：拦截器，获取session中指定数据来判断是否登录

高并发系统关注的问题：服务单一职责独立部署、秒杀链加密、库存预热快速扣减、动静分离、恶意请求拦截、流量错峰、限流熔断降级、队列削峰

定时任务实现非阻塞：定时+异步-->非阻塞

OpenFeign生成的request对象丢失请求头的问题

分布式锁

缓存和数据库数据一致性问题：双写模式、更新就丢弃缓存、将数据库更新的数据放到消息队列异步更新Redis、canal中间件

