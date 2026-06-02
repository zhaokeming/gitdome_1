package org.example.edufix;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * 校园社区综合服务与报修工单平台 - 启动类
 */
@SpringBootApplication
@MapperScan("org.example.edufix.mapper")
public class EdufixApplication {
    
    public static void main(String[] args) {
        // 启用JavaTimeModule
        System.setProperty("spring.jackson.serialization.write-dates-as-timestamps", "false");
        SpringApplication.run(EdufixApplication.class, args);
        System.out.println("========================================");
        System.out.println("校园社区综合服务与报修工单平台启动成功!");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("========================================");
    }
}
