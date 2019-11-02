package com.edu.training.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BaseService {
    @Autowired
    protected IdWorker idWorker;

    @Autowired
    protected FileService fileService;

    @Autowired
    protected UserService userService;

    /**
     * 生成唯一的 64 位 long 的 ID
     *
     * @return 返回唯一 ID
     */
    public long nextId() {
        return idWorker.nextId();
    }
}
