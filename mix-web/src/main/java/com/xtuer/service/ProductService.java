package com.xtuer.service;

import com.xtuer.lock.Lock;
import com.xtuer.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;

@Service
@Slf4j
public class ProductService {
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductService self;

    /**
     * 增加产品的数量
     *
     * @param productId 产品 ID
     * @param inc       增加的数量
     * @param mode      执行模式: 1 (无锁)、2 (DB 锁)、3 (Zookeeper 分布式锁)、4 (注解实现的分布式锁)
     * @return 返回执行时间 (毫秒)
     */
    public long increaseProductCount(int productId, int inc, int mode) throws Exception {
        // 1. 创建 CompletionService 用于执行并且等待所有线程执行结束
        // 2. 创建计时器用于统计吞吐量
        // 3. 每个线程执行一个更新操作
        // 4. 等待所有线程都执行结束
        // 5. 计算执行时间

        log.info("[开始] 增加产品的数量: 产品 [{}], 数量 [{}], 模式 [{}]", productId, inc, mode);

        // [1] 创建 CompletionService 用于执行并且等待所有线程执行结束
        ExecutorService executor = Executors.newFixedThreadPool(100);
        CompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);

        // [2] 创建计时器用于统计吞吐量
        StopWatch sw = StopWatch.createStarted();

        for (int i = 0; i < inc; ++i) {
            // [3] 每个线程执行一个更新操作
            completionService.submit(() -> {
                switch(mode) {
                    case 1: self.increaseProductCountWithoutLock(productId, 1); break;
                    case 2: self.increaseProductCountWithDbLock(productId, 1); break;
                    case 3: self.increaseProductCountWithZookeeperLock(productId, 1); break;
                    case 4: self.increaseProductCountWithAnnotation(productId, 1); break;
                    default:
                        System.out.println("Unknown Mode: " + mode);
                }

                return 0;
            });
        }
        executor.shutdown();

        // [4] 等待所有线程都执行结束
        // [5] 计算执行时间
        for(int i = 0; i < inc; i++) {
            completionService.take().get();
        }

        long elapsed = sw.getTime();
        sw.stop();

        log.info("[完成] 增加产品的数量: 产品 [{}], 数量 [{}], 模式 [{}]，耗时 [{} 毫秒]", productId, inc, mode, elapsed);

        return elapsed;
    }

    /**
     * 增加产品的数量 (无锁)
     *
     * @param productId 产品 ID
     * @param inc       增加的数量
     */
    public void increaseProductCountWithoutLock(int productId, int inc) {
        // 1. 查询产品数量
        // 2. 产品数量增加 inc

        int count = productMapper.findProductCount(productId);
        productMapper.updateProductCount(productId, count + inc);
    }

    /**
     * 增加产品的数量 (DB 锁)
     *
     * @param productId 产品 ID
     * @param inc       增加的数量
     */
    @Transactional(rollbackFor = Exception.class)
    public void increaseProductCountWithDbLock(int productId, int inc) {
        // 1. 锁住产品
        // 2. 查询产品数量
        // 3. 产品数量增加 inc

        productMapper.lockProduct(productId);
        int count = productMapper.findProductCount(productId);
        productMapper.updateProductCount(productId, count + inc);
    }

    /**
     * 增加产品的数量 (Zookeeper 分布式锁)
     *
     * @param productId 产品 ID
     * @param inc       增加的数量
     */
    // @Lock("'/lock/product' + #productId") // /lock/product-1
    public void increaseProductCountWithZookeeperLock(int productId, int inc) {
        // 1. 获取分布式锁
        // 2. 查询产品数量
        // 3. 产品数量增加 inc
        // 4. 释放分布式锁

        String lockName = "/lock/product-" + productId;
        InterProcessMutex lock = new InterProcessMutex(client, lockName);

        try {
            // [1] 获取分布式锁
            if (lock.acquire(10, TimeUnit.SECONDS)) {
                try {
                    // [2] 查询产品数量
                    // [3] 产品数量增加 1
                    int count = productMapper.findProductCount(productId);
                    productMapper.updateProductCount(productId, count + inc);
                } finally {
                    // [4] 释放分布式锁
                    lock.release();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加产品的数量 (注解实现的分布式锁)
     *
     * @param productId 产品 ID
     * @param inc       增加的数量
     */
    @Lock("'/lock/product-' + #productId")
    public void increaseProductCountWithAnnotation(int productId, int inc) {
        // 1. 查询产品数量
        // 2. 产品数量增加 inc

        int count = productMapper.findProductCount(productId);
        productMapper.updateProductCount(productId, count + inc);
    }

    /**
     * Curator 客户端，用于连接 Zookeeper
     */
    private CuratorFramework client;

    /**
     * 初始化时连接 Zookeeper
     */
    // @PostConstruct
    public void connectToZookeeper() {
        client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new ExponentialBackoffRetry(10000, 3));
        client.start();
    }

    /**
     * 程序退出时断开和 Zookeeper 的连接
     */
    @PreDestroy
    public void disconnectFromZookeeper() {
        client.close();
    }
}
