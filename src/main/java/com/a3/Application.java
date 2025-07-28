package com.a3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    // Game backend server. This MUST be running for anything involving the browser frontend.
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
