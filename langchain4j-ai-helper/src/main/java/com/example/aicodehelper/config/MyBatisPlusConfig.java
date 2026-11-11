package com.example.aicodehelper.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.aicodehelper.mapper")
public class MyBatisPlusConfig {
}
