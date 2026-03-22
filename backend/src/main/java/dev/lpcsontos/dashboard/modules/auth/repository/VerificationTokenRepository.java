package dev.lpcsontos.dashboard.modules.auth.repository;

import dev.lpcsontos.dashboard.modules.auth.domain.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    void deleteByUserId(Long userId);
}

