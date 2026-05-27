package com.example.springmodulith;

import com.example.springmodulith.common.RsaKeyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties(RsaKeyProperties.class)
@EnableScheduling
@SpringBootApplication
public class SpringModulithApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringModulithApplication.class, args);
    }

}
