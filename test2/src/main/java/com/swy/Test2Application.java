package com.swy;

import com.example.common.DistTransApplication;
import com.example.common.RestTemplateFactory;
import com.jds.core.common.ServiceLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@ComponentScan(basePackages = {"com.swy.*", "com.example.*", "com.jds.*"})
//@SpringCloudApplication
@DistTransApplication
public class Test2Application {
    @Autowired
    private RestTemplateFactory restTemplateFactory;

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return restTemplateFactory.getRestTemplate();
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(Test2Application.class, args);

        ServiceLocator locator = new ServiceLocator();
        locator.setApplicationContext(ctx);
    }

}
