package dev.lpcsontos.dashboard.modules.auth;

import dev.lpcsontos.dashboard.modules.auth.dto.RegisterRequest;
import dev.lpcsontos.dashboard.modules.auth.dto.RefreshRequest;
import dev.lpcsontos.dashboard.modules.auth.exception.EmailAlreadyUsedException;
import dev.lpcsontos.dashboard.modules.auth.exception.InvalidTokenException;
import dev.lpcsontos.dashboard.modules.auth.exception.TokenExpiredException;
import dev.lpcsontos.dashboard.modules.auth.repository.VerificationTokenRepository;
import dev.lpcsontos.dashboard.modules.auth.service.AuthService;
import dev.lpcsontos.dashboard.modules.auth.service.RefreshTokenService;
import dev.lpcsontos.dashboard.modules.user.domain.User;
import dev.lpcsontos.dashboard.modules.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService auth;
    private UserRepository users;
    private VerificationTokenRepository tokens;
    private PasswordEncoder encoder;
    private AuthenticationManager authManager;
    private JavaMailSender mail;
    private RefreshTokenService refreshTokens;

    @BeforeEach
    void setup() {
        users = Mockito.mock(UserRepository.class);
        tokens = Mockito.mock(VerificationTokenRepository.class);
        encoder = Mockito.mock(PasswordEncoder.class);
        authManager = Mockito.mock(AuthenticationManager.class);
        mail = Mockito.mock(JavaMailSender.class);
        refreshTokens = Mockito.mock(RefreshTokenService.class);

        auth = new AuthService(users, tokens, encoder, authManager, null, mail, refreshTokens);
    }

    @Test
    void register_shouldThrowIfEmailExists() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com");
        req.setPassword("pass");

        Mockito.when(users.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyUsedException.class, () -> auth.register(req));
    }

    @Test
    void refresh_shouldThrowIfTokenInvalid() {
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("invalid");

        Mockito.when(refreshTokens.findUserIdByToken("invalid"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> auth.refresh(req));
    }

    @Test
    void verifyAccount_shouldThrowIfExpired() {
        var vt = new dev.lpcsontos.dashboard.modules.auth.domain.VerificationToken();
        vt.setExpiryDate(LocalDateTime.now().minusMinutes(1));

        Mockito.when(tokens.findByToken("abc")).thenReturn(Optional.of(vt));

        assertThrows(TokenExpiredException.class, () -> auth.verifyAccount("abc"));
    }
}
