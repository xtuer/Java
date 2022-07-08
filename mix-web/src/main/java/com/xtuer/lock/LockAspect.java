package com.xtuer.lock;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁的切面
 */
@Aspect
@Slf4j
@Component
public class LockAspect {
    /**
     * 通过 @annotation 来配置切点，表示我们定义的切面 LockAspect 会切入到所有用注解 @Lock 修饰的方法上，
     * 也就是那些方法执行时会使用下面定义的 @Around
     */
    @Pointcut("@annotation(com.xtuer.lock.Lock)")
    public void pointcut() {}

    /**
     * 使用分布式锁保护函数的调用
     */
    @Around("pointcut() && @annotation(lock)")
    public Object doAround(ProceedingJoinPoint pjp, Lock lock) throws Throwable {
        // 1. 构建锁的名字
        // 2. 获取分布式锁
        // 3. 执行业务逻辑
        // 4. 释放分布式锁

        // [1] 构建锁的名字
        String lockName = buildLockName(pjp, lock);

        // [2] 获取分布式锁
        InterProcessMutex mutex = new InterProcessMutex(client, lockName);
        if (mutex.acquire(10, TimeUnit.SECONDS)) {
            log.debug("[分布式锁] 获得锁: [{}]", lockName);

            try {
                // [3] 执行业务逻辑
                return pjp.proceed();
            } finally {
                // [4] 释放分布式锁
                mutex.release();
                log.debug("[分布式锁] 释放锁: [{}]", lockName);
            }
        }

        throw new RuntimeException("获取分布式锁超时");
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

    private final ExpressionParser parser = new SpelExpressionParser();
    private final LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();

    // 使用 SpEL 表达式创建锁的名字
    private String buildLockName(ProceedingJoinPoint pjp, Lock lock) {
        // 1. 如果 lock.value() 中不包含 #，则说明只是一个普通的字符串，直接返回作为锁的名字
        // 2. 获取被调用的方法以及它的参数名和参数值
        // 3. 把参数名和对应的参数值设置到 context 中
        // 4. 创建执行 SpEL 表达式, 并返回它的结果作为锁的名字

        String spel = lock.value();

        // [1] 如果 lock.value() 中不包含 #，则说明只是一个普通的字符串，直接返回作为锁的名字
        if (!spel.contains("#")) {
            return spel;
        }

        // [2] 获取被调用的方法以及它的参数名和参数值
        Method method   = ((MethodSignature) pjp.getSignature()).getMethod(); // 方法
        String[] params = discoverer.getParameterNames(method);               // 参数名
        Object[] args   = pjp.getArgs();                                      // 参数值
        EvaluationContext context = new StandardEvaluationContext();

        // [3] 把参数名和对应的参数值设置到 context 中
        if (params != null) {
            for (int len = 0; len < params.length; len++) {
                context.setVariable(params[len], args[len]);
            }
        }

        // [4] 创建执行 SpEL 表达式, 并返回它的结果作为锁的名字
        Expression expression = parser.parseExpression(spel);
        return expression.getValue(context, String.class);
    }
}
