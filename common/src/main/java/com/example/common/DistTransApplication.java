package com.example.common;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @Author : Meryl
 * @Description:
 * @Date: Created in 2020/1/3
 * @Modify by :
 */
@Target({ElementType.TYPE})
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@EnableDiscoveryClient
@EnableCircuitBreaker
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@Import(DistributedTransImportSelector.class)
public @interface DistTransApplication {
    boolean enabled() default true;
}
