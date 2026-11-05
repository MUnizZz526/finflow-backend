package com.finflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FinFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinFlowApplication.class, args);
    }
}

