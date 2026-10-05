package com.forgeci.infrastructure.organization;
import com.forgeci.domain.organization.Organization;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OrganizationRepository extends JpaRepository<Organization,UUID> {
 Optional<Organization> findBySlug(String slug);
}
