package com.example.privattest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PrivatTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrivatTestApplication.class, args);
    }

}
