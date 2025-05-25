package com.example.spring_boot.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EntityScan("com.example.spring_boot.entity")
public class JpaConfig {}
