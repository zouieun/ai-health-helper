package com.example.aihealthhelper.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.aihealthhelper.mapper")
public class MyBatisPlusConfig {
}
