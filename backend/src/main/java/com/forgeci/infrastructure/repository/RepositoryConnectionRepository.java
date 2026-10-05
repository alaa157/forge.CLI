package com.forgeci.infrastructure.repository;
import com.forgeci.domain.repository.RepositoryConnection;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RepositoryConnectionRepository extends JpaRepository<RepositoryConnection,UUID> {
 List<RepositoryConnection> findAllByOrganizationId(UUID organizationId);
 Optional<RepositoryConnection> findByIdAndOrganizationId(UUID id,UUID organizationId);
 Optional<RepositoryConnection> findByOrganizationIdAndProviderAndExternalId(UUID organizationId,com.forgeci.domain.repository.RepositoryProvider provider,String externalId);
}
