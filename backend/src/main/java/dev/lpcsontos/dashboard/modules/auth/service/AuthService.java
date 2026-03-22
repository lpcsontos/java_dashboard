package dev.lpcsontos.dashboard.modules.auth.service;

import dev.lpcsontos.dashboard.modules.auth.domain.VerificationToken;
import dev.lpcsontos.dashboard.modules.auth.dto.*;
import dev.lpcsontos.dashboard.modules.auth.exception.EmailAlreadyUsedException;
import dev.lpcsontos.dashboard.modules.auth.exception.InvalidTokenException;
import dev.lpcsontos.dashboard.modules.auth.exception.TokenExpiredException;
import dev.lpcsontos.dashboard.modules.auth.exception.UserNotFoundException;
import dev.lpcsontos.dashboard.modules.auth.repository.VerificationTokenRepository;
import dev.lpcsontos.dashboard.modules.user.domain.User;
import dev.lpcsontos.dashboard.modules.user.repository.UserRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository users;
    private final VerificationTokenRepository tokens;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final JavaMailSender mailSender;
    private final RefreshTokenService refreshTokens;


    public AuthService(UserRepository users,
                       VerificationTokenRepository tokens,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authManager,
                       JwtService jwtService,
                       JavaMailSender mailSender,
                       RefreshTokenService refreshTokens) {

        this.users = users;
        this.tokens = tokens;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.mailSender = mailSender;
        this.refreshTokens = refreshTokens;
    }


    private String createVerificationToken(User user) {
        tokens.deleteByUserId(user.getId());

        String token = java.util.UUID.randomUUID().toString();

        VerificationToken vt = new VerificationToken();
        vt.setToken(token);
        vt.setUser(user);
        vt.setExpiryDate(LocalDateTime.now().plusHours(24));

        tokens.save(vt);
        return token;
    }

    private void sendVerificationEmail(String email, String token) {
        String link = "http://localhost:8080/api1/auth/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom("no-reply@dashboard.local");
        message.setSubject("Verify your account");
        message.setText("Click the link to verify your account:\n" + link);

        mailSender.send(message);
    }

    public void resendVerification(ResendVerificationRequest request) {
        User user = users.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.isEnabled()) {
            throw new EmailAlreadyUsedException("User is already verified");
        }

        tokens.deleteByUserId(user.getId());

        String token = createVerificationToken(user);
        sendVerificationEmail(user.getEmail(), token);
    }


    public void register(RegisterRequest request) {
        if (users.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyUsedException("Email already in use");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false);
        user.setRoles(Set.of("ROLE_USER"));
        user.setProvider("LOCAL");

        users.save(user);

        String token = createVerificationToken(user);
        sendVerificationEmail(user.getEmail(), token);
    }


    public JwtResponse login(LoginRequest request) {
        var authToken = new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
        );
        authManager.authenticate(authToken);

        User user = users.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = UUID.randomUUID().toString();

        refreshTokens.storeRefreshToken(user.getId(), refreshToken);

        return new JwtResponse(accessToken, refreshToken);
    }

    public void logout(RefreshRequest request) {
        Long userId = refreshTokens.findUserIdByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        refreshTokens.deleteRefreshToken(userId);
    }

    public JwtResponse refresh(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        // Extract user ID from refresh token storage
        Long userId = refreshTokens.findUserIdByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (!refreshTokens.validateRefreshToken(userId, refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        User user = users.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String newAccessToken = jwtService.generateToken(user.getEmail());

        return new JwtResponse(newAccessToken, refreshToken);
    }

    public void verifyAccount(String token) {
        VerificationToken vt = tokens.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Verification token expired");
        }

        User user = vt.getUser();
        user.setEnabled(true);
        users.save(user);

        tokens.delete(vt);
    }

}
