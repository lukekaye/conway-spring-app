package com.example.spring_boot.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for every test that needs a real database.
 *
 * <p>The container is a static field, started once in a static initialiser
 * and shared by every subclass in the JVM. Do not annotate it {@code @Container}:
 * that lifecycle stops and restarts the container per test class, which turns
 * a 30-second suite into several minutes. Testcontainers' Ryuk sidecar removes
 * the container when the JVM exits, so there is nothing to stop by hand.
 *
 * <p>{@code @ServiceConnection} (Spring Boot 3.1+) wires the JDBC URL, username,
 * and password into the context automatically — no {@code spring.datasource.*}
 * properties are needed anywhere in the test resources.
 */
@Testcontainers
public abstract class AbstractDatabaseIT {

    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0");

    static {
        MYSQL.start();
    }
}
