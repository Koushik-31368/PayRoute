package com.payroute;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * PayRoute â€” Intelligent Payment Orchestration Engine
 *
 * @EnableScheduling is needed for the circuit breaker's HALF_OPEN trickle timer
 * and any future periodic health checks.
 */
@SpringBootApplication
@EnableScheduling
/** @author PayRoute Contributors */
public class PayRouteApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayRouteApplication.class, args);
    }
}

