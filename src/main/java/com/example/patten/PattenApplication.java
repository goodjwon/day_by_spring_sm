package com.example.patten;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Minimal Spring configuration root for the "patten" project space only.
 * This keeps the patten package isolated from the spring application.
 */
@SpringBootConfiguration
@ComponentScan(basePackages = "com.example.patten")
public class PattenApplication {
    // Test-only lightweight configuration root for com.example.patten.*
}
