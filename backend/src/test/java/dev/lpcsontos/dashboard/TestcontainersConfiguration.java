package dev.lpcsontos.dashboard;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@TestConfiguration
public class TestcontainersConfiguration {

    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("dashboard_db")
                    .withUsername("dashboard_user")
                    .withPassword("test");

    @ServiceConnection
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7")
                    .withExposedPorts(6379);

    @ServiceConnection
    static GenericContainer<?> mailhog =
            new GenericContainer<>("mailhog/mailhog")
                    .withExposedPorts(1025, 8025);
}
