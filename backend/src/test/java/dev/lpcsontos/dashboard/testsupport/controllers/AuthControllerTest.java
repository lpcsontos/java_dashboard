package dev.lpcsontos.dashboard.testsupport.controllers;

import dev.lpcsontos.dashboard.modules.auth.controller.AuthController;
import dev.lpcsontos.dashboard.modules.auth.dto.JwtResponse;
import dev.lpcsontos.dashboard.modules.auth.service.AuthService;
import dev.lpcsontos.dashboard.modules.auth.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    AuthService auth;

    @Test
    void register_shouldReturn200() throws Exception {
        Mockito.doNothing().when(auth).register(Mockito.any());

        mvc.perform(post("/api1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "test@example.com",
                          "password": "password123"
                        }
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void login_shouldReturn200() throws Exception {
        Mockito.when(auth.login(Mockito.any()))
                .thenReturn(new JwtResponse("access", "refresh"));

        mvc.perform(post("/api1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "test@example.com",
                          "password": "password123"
                        }
                        """))
                .andExpect(status().isOk());
    }
}
