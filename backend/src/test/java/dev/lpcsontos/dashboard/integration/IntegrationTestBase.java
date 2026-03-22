package dev.lpcsontos.dashboard.integration;

import dev.lpcsontos.dashboard.TestcontainersConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
        "management.health.mail.enabled=false",
        "spring.mail.host=disabled",
        "spring.mail.port=0",
        "jwt.secret=TEST_SECRET",
        "jwt.expiration=3600000"
})
public abstract class IntegrationTestBase {
}