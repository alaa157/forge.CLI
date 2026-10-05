package com.forgeci.infrastructure.organization;
import com.forgeci.domain.organization.Organization; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface OrganizationRepository extends JpaRepository<Organization,UUID>{Optional<Organization> findBySlug(String slug);}
