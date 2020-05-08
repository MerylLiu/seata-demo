package com.example.common;

import com.example.common.config.RestTemplateHttpRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Component
public class RestTemplateFactory {

    @Autowired
    RestTemplateHttpRequestInterceptor restTemplateHttpRequestInterceptor;

    public RestTemplate getRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        BufferingClientHttpRequestFactory bufferingClientHttpRequestFactory = new BufferingClientHttpRequestFactory(factory);

        RestTemplate restTemplate = new RestTemplate(bufferingClientHttpRequestFactory);
        restTemplate.getInterceptors().add(restTemplateHttpRequestInterceptor);
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }
}
