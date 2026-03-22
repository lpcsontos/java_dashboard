package dev.lpcsontos.dashboard.integration.security;

import dev.lpcsontos.dashboard.integration.IntegrationTestBase;
import dev.lpcsontos.dashboard.modules.user.domain.User;
import dev.lpcsontos.dashboard.modules.user.repository.UserRepository;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled("Roles/admin not implemented yet")
class RoleSecurityIntegrationTest extends IntegrationTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository users;

    @Autowired
    PasswordEncoder encoder;

    @Test
    void adminEndpointRequiresAdminRole() throws Exception {

        // Create normal user
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword(encoder.encode("password123"));
        user.setEnabled(true);
        user.setProvider("LOCAL");
        user.setRoles(Set.of("ROLE_USER"));
        users.save(user);

        // Login
        var login = mvc.perform(post("/api1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = login.getResponse().getContentAsString().split("\"")[3];

        // Try admin endpoint
        mvc.perform(get("/api1/admin/users")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }
}
