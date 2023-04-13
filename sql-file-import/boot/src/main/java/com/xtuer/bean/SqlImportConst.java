package com.xtuer.bean;

/**
 * 导入 SQL 文件状态常量。
 */
public interface SqlImportConst {
    /**
     * 初始化状态。
     */
    int INIT = 0;

    /**
     * 导入成功。
     */
    int SUCCESS = 1;

    /**
     * 导入失败。
     */
    int FAILED = 2;

    /**
     * 导入中。
     */
    int DOING = 3;

    /**
     * 排队等待。
     */
    int WAIT = 4;
}
