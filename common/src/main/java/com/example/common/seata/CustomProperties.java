package com.example.common.seata;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = CustomProperties.PREFIX)
public class CustomProperties {

    public static final String PREFIX = "seata.config.custom";
    private String name = "SeataConfig";
}
