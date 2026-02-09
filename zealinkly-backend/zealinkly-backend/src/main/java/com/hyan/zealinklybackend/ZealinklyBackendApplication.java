package com.hyan.zealinklybackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ZealinklyBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZealinklyBackendApplication.class, args);
    }

}
