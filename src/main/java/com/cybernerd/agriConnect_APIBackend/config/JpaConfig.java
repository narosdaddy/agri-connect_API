package com.cybernerd.agriConnect_APIBackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.cybernerd.agriConnect_APIBackend.repository")
public class JpaConfig {
} 