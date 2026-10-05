package com.forgeci.infrastructure.organization;
import com.forgeci.domain.organization.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember,UUID> {
 Optional<OrganizationMember> findByOrganizationIdAndUserId(UUID organizationId,UUID userId);
 List<OrganizationMember> findAllByUserId(UUID userId);
 List<OrganizationMember> findAllByOrganizationId(UUID organizationId);
 boolean existsByOrganizationIdAndUserId(UUID organizationId,UUID userId);
}
