package dev.lpcsontos.dashboard.integration.auth;

import dev.lpcsontos.dashboard.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import dev.lpcsontos.dashboard.modules.auth.domain.VerificationToken;
import dev.lpcsontos.dashboard.modules.auth.repository.VerificationTokenRepository;
import dev.lpcsontos.dashboard.modules.user.repository.UserRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, AuthFlowIntegrationTest.MailConfig.class})
class AuthFlowIntegrationTest {

    @Autowired
    MockMvc mvc;

    @TestConfiguration
    static class MailConfig {
        @Bean
        JavaMailSender javaMailSender() {
            return Mockito.mock(JavaMailSender.class);
        }
    }

    @Autowired UserRepository users;
    @Autowired VerificationTokenRepository tokens;

    @Test
    void fullAuthFlow() throws Exception {
        String email = "flow+" + UUID.randomUUID() + "@example.com";

        mvc.perform(post("/api1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "%s",
                          "password": "password123"
                        }
                        """.formatted(email)))
                .andExpect(status().isOk());

        assertThat(users.existsByEmail(email)).isTrue();

        VerificationToken token = tokens.findAll().get(0);

        mvc.perform(get("/api1/auth/verify")
                        .param("token", token.getToken()))
                .andExpect(status().isOk());

        assertThat(users.findByEmail(email).get().isEnabled()).isTrue();

        var login = mvc.perform(post("/api1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "%s",
                          "password": "password123"
                        }
                        """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();

        String body = login.getResponse().getContentAsString();
        String accessToken = body.split("\"")[3];
        String refreshToken = body.split("\"")[7];

        mvc.perform(post("/api1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(refreshToken)))
                .andExpect(status().isOk());

        mvc.perform(post("/api1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(refreshToken)))
                .andExpect(status().isOk());
    }
}
