package com.xtuer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

// import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class TokenWhiteListService {
    // @Resource(name = "whiteList")
    private Set<String> whiteList;

    public void foo() {
        System.out.println(whiteList);
        System.out.println(whiteList.size());
    }

    @Bean
    public List<String> ns() {
        log.info("@Bean ns()");
        return Arrays.asList("One", "Two");
    }
}
