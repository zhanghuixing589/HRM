package org.example.hrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "org.example.hrm")
public class HrmApplication {

    public static void main(String[] args) {
        
        SpringApplication.run(HrmApplication.class, args);
        System.out.println( "HRM Application started successfully!");

    }

}
