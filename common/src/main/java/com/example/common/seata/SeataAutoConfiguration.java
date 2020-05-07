package com.example.common.seata;


import config.YamlConfigFactory;
import io.seata.config.springcloud.EnableSeataSpringConfig;
import io.seata.spring.annotation.GlobalTransactionScanner;
import io.seata.spring.annotation.datasource.EnableAutoDataSourceProxy;
import io.seata.spring.boot.autoconfigure.properties.SeataProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {"classpath:seata.yml"}, factory = YamlConfigFactory.class)
@EnableAutoDataSourceProxy
@EnableSeataSpringConfig
@EnableConfigurationProperties({CustomProperties.class})
public class SeataAutoConfiguration {
    @Autowired
    private SeataProperties seataProperties;

    @Bean
    public SpringUtils springUtils() {
        return new SpringUtils();
    }

    @Bean
    @DependsOn({"springUtils"})
    @ConditionalOnMissingBean(GlobalTransactionScanner.class)
    public GlobalTransactionScanner globalTransactionScanner() {
        return new GlobalTransactionScanner(seataProperties.getApplicationId(), seataProperties.getTxServiceGroup());
    }
}
