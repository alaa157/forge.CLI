package com.forgeci.infrastructure.organization;
import com.forgeci.domain.organization.GitHubConnection;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface GitHubConnectionRepository extends JpaRepository<GitHubConnection,UUID> {
 List<GitHubConnection> findAllByOrganizationId(UUID organizationId);
 Optional<GitHubConnection> findByIdAndOrganizationId(UUID id,UUID organizationId);
 Optional<GitHubConnection> findByOrganizationIdAndExternalAccountId(UUID organizationId,String externalAccountId);
}
