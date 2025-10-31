package com.location.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class LocationManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocationManagementApplication.class, args);
    }
}
