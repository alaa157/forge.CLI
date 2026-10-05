package com.forgeci.application.organization;

import com.forgeci.application.auth.AuthException;
import com.forgeci.domain.organization.Organization;
import com.forgeci.domain.organization.OrganizationMember;
import com.forgeci.domain.organization.OrganizationRole;
import com.forgeci.infrastructure.organization.OrganizationMemberRepository;
import com.forgeci.infrastructure.organization.OrganizationRepository;
import com.forgeci.infrastructure.user.UserRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationService {
    private final OrganizationRepository organizations;
    private final OrganizationMemberRepository members;
    private final UserRepository users;

    public OrganizationService(
            OrganizationRepository o, OrganizationMemberRepository m, UserRepository u) {
        organizations = o;
        members = m;
        users = u;
    }

    @Transactional
    public Organization create(UUID userId, String name, String slug) {
        if (name == null || name.isBlank() || name.length() > 120) {
            throw new AuthException("INVALID_ORGANIZATION", "Organization name is required");
        }
        slug = slugify(slug);
        if (slug.isBlank() || slug.length() > 80) {
            throw new AuthException("INVALID_ORGANIZATION", "Invalid organization slug");
        }
        if (organizations.findBySlug(slug).isPresent()) {
            throw new AuthException("DUPLICATE_ORGANIZATION", "Organization slug already exists");
        }
        var user = users.findById(userId).orElseThrow(() -> new AuthException("USER_NOT_FOUND", "User not found"));
        var org = organizations.save(new Organization(name.trim(), slug, user));
        members.save(new OrganizationMember(org, user, OrganizationRole.OWNER));
        return org;
    }

    @Transactional(readOnly = true)
    public List<Organization> listForUser(UUID userId) {
        return members.findAllByUserId(userId).stream()
                .map(OrganizationMember::getOrganization)
                .toList();
    }

    public boolean canManage(UUID userId, UUID orgId) {
        return members.findByOrganizationIdAndUserId(orgId, userId)
                .map(m -> m.getRole() == OrganizationRole.OWNER || m.getRole() == OrganizationRole.ADMIN)
                .orElse(false);
    }

    public boolean isMember(UUID userId, UUID orgId) {
        return members.findByOrganizationIdAndUserId(orgId, userId).isPresent();
    }

    private String slugify(String s) {
        if (s == null) {
            return "";
        }
        return s.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}
