package com.keon.todoapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Todo 애플리케이션의 메인 클래스
 * Spring Boot 애플리케이션의 진입점 역할을 합니다.
 */
@SpringBootApplication
public class TodoAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoAppApplication.class, args);
    }
}
