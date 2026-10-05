package com.forgeci.infrastructure.organization;
import com.forgeci.domain.organization.OAuthState;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
public interface OAuthStateRepository extends JpaRepository<OAuthState,UUID> {
 @Lock(LockModeType.PESSIMISTIC_WRITE) Optional<OAuthState> findByStateHash(String stateHash);
}
