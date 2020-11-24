package com.karson.mall.search.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Karson
 */
public class ThreadTest {
    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main---start---");
        //创建启动异步任务
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程" + Thread.currentThread().getId());
//            int i = 10 / 0;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, executor).whenComplete((result, exception) -> {
//            //虽然能得到异常信息，但没法修改返回数据
//            System.out.println("异步任务完成---result:" + result + "---exception:" + exception);
//        }).exceptionally(throwable -> {
//            //可以感知异常，同时返回默认值
//            return 10;
//        });

        /**
         * 方法执行完成后的处理
         */
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, executor).handle((result, throwable) -> {
//            if (result != null) {
//                return result * 2;
//            }
//            if (throwable != null) {
//                return 0;
//            }
//            return 0;
//        });


//        /**
//         * 线程串行化
//         */
//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, executor).thenApplyAsync(res -> {
//
//            return "Hello" + res;
//        }, executor);


        CompletableFuture<Object> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("future1当前线程" + Thread.currentThread().getId());
            int i = 10 / 4;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("future1运行结果：" + i);
            return i;
        }, executor);
        CompletableFuture<Object> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("future2当前线程" + Thread.currentThread().getId());
            System.out.println("future2运行结束");
            return "Hello";
        }, executor);

//        future1.runAfterBothAsync(future2, () -> {
//            System.out.println("future3运行开始");
//        }, executor);


//        future1.thenAcceptBothAsync(future2, (res1, res2) -> {
//            System.out.println(res1 + res2);
//        }, executor);
//        CompletableFuture<String> future = future1.thenCombineAsync(future2, (res1, res2) -> {
//            System.out.println(res1 + res2);
//            return res1 + res2;
//        }, executor);
        /**
         * 两个任务只要有一个完成
         */
//        future1.runAfterEitherAsync(future2, () -> {
//            System.out.println("future3运行开始");
//        }, executor);

        future1.acceptEitherAsync(future2, (res) -> {
            System.out.println("haksldaj"+res);
        }, executor);
        System.out.println("main---end----");
    }
}
