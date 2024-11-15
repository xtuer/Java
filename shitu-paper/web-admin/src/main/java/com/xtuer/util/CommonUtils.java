package com.xtuer.util;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.UUID;

public final class CommonUtils {
    /**
     * BindingResult 中的错误信息很多，对用户不够友好，使用 getBindingMessage()
     * 提取对用户阅读友好的定义验证规则 message。
     *
     * @param result 验证的结果对象
     * @return 验证规则 message
     */
    public static String getBindingMessage(BindingResult result) {
        StringBuffer sb = new StringBuffer();

        for (FieldError error : result.getFieldErrors()) {
            // sb.append(error.getField() + " : " + error.getDefaultMessage() + "\n");
            sb.append(error.getDefaultMessage() + "\n");
        }

        return sb.toString();
    }

    /**
     * 生成 UUID
     * @return 返回 UUID
     */
    public static String uuid() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    /**
     * 使用 UUID 或则 UUID 的文件名生成目录名字，为 [0, 99]。
     * 算法为 UUID 的 hashCode 的绝对值模 100。
     *
     * @param uuid uuid UUID 或则 UUID 的文件名如 UUID.doc
     * @return 目录名字
     */
    public static String directoryNameByUuid(String uuid) {
        uuid = uuid.trim();
        uuid = uuid.replaceAll("^(\\w+)(\\..*)", "$1"); // 去掉 . 和其后面部分

        return Math.abs(uuid.hashCode()) % 100 + "";
    }
}
