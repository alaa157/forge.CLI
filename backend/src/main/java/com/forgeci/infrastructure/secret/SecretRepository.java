package com.forgeci.infrastructure.secret;
import com.forgeci.domain.secret.Secret; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface SecretRepository extends JpaRepository<Secret,UUID>{Optional<Secret> findByOrganizationIdAndName(UUID organizationId,String name);List<Secret> findAllByOrganizationId(UUID organizationId);}