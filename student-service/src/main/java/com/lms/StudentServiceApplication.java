package com.lms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.lms.client")
public class StudentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudentServiceApplication.class, args);
    }
}