package com.forgeci.infrastructure.user;

import com.forgeci.domain.user.RefreshToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken t set t.revokedAt = CURRENT_TIMESTAMP where t.familyId = :familyId and t.revokedAt is null")
    int revokeFamily(UUID familyId);

    List<RefreshToken> findAllByFamilyId(UUID familyId);
}
