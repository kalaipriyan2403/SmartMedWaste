package com.smartmedwaste.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        // Time to establish HTTPS connection to Render
        factory.setConnectTimeout(600_000);

        // Render + AI inference can take time
        factory.setReadTimeout(800_000);

        return new RestTemplate(factory);
    }
}