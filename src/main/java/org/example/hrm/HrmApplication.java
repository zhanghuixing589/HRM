package org.example.hrm;

import javax.persistence.EntityManagerFactory;

import org.hibernate.Metamodel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@ComponentScan(basePackages = "org.example.hrm")
@Slf4j
public class HrmApplication {

    public static void main(String[] args) {
        
        SpringApplication.run(HrmApplication.class, args);
        System.out.println( "HRM Application started successfully!");

    }

     @Bean
    public CommandLineRunner debugEntityManager(EntityManagerFactory emf) {
        return args -> {
            log.info("验证实体类映射...");
            Metamodel metamodel = (Metamodel) emf.getMetamodel();
            metamodel.getEntities().forEach(entityType -> {
                log.info("Entity: {}", entityType.getName());
                entityType.getAttributes().forEach(attr -> {
                    log.info("  Attribute: {} -> {}", attr.getName(), attr.getJavaType());
                });
            });
        };
    }

}
