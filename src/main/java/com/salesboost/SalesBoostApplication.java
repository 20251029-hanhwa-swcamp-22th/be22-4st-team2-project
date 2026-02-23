package com.salesboost;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SalesBoostApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalesBoostApplication.class, args);
    }
}
