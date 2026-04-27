package com.hauen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class HauenApplication {

    public static void main(String[] args) {
        SpringApplication.run(HauenApplication.class, args);
    }

}
