package com.exam.bean;

/**
 * 缓存使用的键和缓存的名字
 */
public interface CacheConst {
    String CACHE = "exam:"; // 默认的缓存对象

    String KEY_USER = "'user.' + #userId";
    String KEY_ORG  = "'org.' + #host";
    String KEY_EXAM = "'exam.' + #examId";
}
