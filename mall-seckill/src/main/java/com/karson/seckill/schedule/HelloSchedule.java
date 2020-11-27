package com.karson.seckill.schedule;

/**
 * @author Karson
 * @EnableScheduling 开启定时任务
 *
 * 定时任务
 *      1.@EnableScheduling 开启定时任务
 *      2.@Sechduled 开启一个定时任务
 *      3.自动配置类：TaskSchedulingAutoConfiguration
 *
 * 异步任务
 *      1.@EnableAsnc 开启异步任务功能 将任务提交给一个线程池
 *      2.@Aysnc 给希望异步执行的方法上标注
 *      3.自动配置类 TaskExecutionAutoConfiguration 属性绑定在TaskExecutorProperties里面
 *
 * 定时任务搭配异步任务  实现定时任务不阻塞
 */
//@EnableAsync
//@EnableScheduling
//@Component
//@Slf4j
public class HelloSchedule {

    /**
     * Spring中6位组成，不允许第7位的年
     * 周周一到周日 1-7
     * 定时任务不应该阻塞,定时任务默认是阻塞的。
     *        1.使用异步编排。ComputableFuture。提交到线程池
     *        2.SpringBoot支持定时任务线程池。通过设置SpringTaskProperties,不太好使
     *        3.让定时任务异步执行
     */
//    @Async
//    @Scheduled(cron = "* * * ? * 5")
//    public void hello() throws InterruptedException {
//        log.info("hello.......");
//        Thread.sleep(3000);
//    }
}
