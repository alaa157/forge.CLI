package com.forgeci.infrastructure.repository;
import com.forgeci.domain.repository.Repository; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface RepositoryRepository extends JpaRepository<Repository,UUID>{List<Repository> findAllByOrganizationId(UUID organizationId); Optional<Repository> findByOrganizationIdAndProviderAndExternalId(UUID organizationId,com.forgeci.domain.repository.RepositoryProvider provider,String externalId);}
