package org.example.dcdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "ark")
public class DoubaoConfig {
    private String apiKey;
    private String endpointId;
} 