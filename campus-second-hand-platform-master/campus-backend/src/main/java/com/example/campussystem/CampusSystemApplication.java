package com.example.campussystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.example.campussystem.mapper")
@EnableScheduling
@EnableAspectJAutoProxy
public class CampusSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusSystemApplication.class, args);
    }

}