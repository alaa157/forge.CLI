package com.forgeci.infrastructure.organization;
import com.forgeci.domain.organization.OrganizationMember; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember,UUID>{Optional<OrganizationMember> findByOrganizationIdAndUserId(UUID organizationId,UUID userId); List<OrganizationMember> findAllByUserId(UUID userId);}
