package com.forgeci.api;

import com.forgeci.application.repository.RepositoryService;
import com.forgeci.domain.repository.RepositoryConnection;
import com.forgeci.infrastructure.repository.RepositoryConnectionRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 15 top-level /repositories resources (aliases of org-scoped controller).
 * Plan: GET /repositories, POST /repositories/{id}/connect, DELETE /repositories/{id}/connect
 */
@RestController
@RequestMapping("/api/v1/repositories")
public class RepositoryResourceController {
    private final RepositoryService service;
    private final RepositoryConnectionRepository repositories;

    public RepositoryResourceController(
            RepositoryService service, RepositoryConnectionRepository repositories) {
        this.service = service;
        this.repositories = repositories;
    }

    @GetMapping
    public List<Response> list(Authentication a, @RequestParam UUID organizationId) {
        return service.list(uid(a), organizationId).stream().map(Response::from).toList();
    }

    @GetMapping("/{id}")
    public Response get(@PathVariable UUID id) {
        return Response.from(
                repositories.findById(id).orElseThrow(() -> new NoSuchElementException("Repository not found")));
    }

    /** Connect is org-scoped; this alias refreshes an already-linked repository. */
    @PostMapping("/{id}/connect")
    public Response connect(Authentication a, @PathVariable UUID id) throws Exception {
        RepositoryConnection repo =
                repositories.findById(id).orElseThrow(() -> new NoSuchElementException("Repository not found"));
        return Response.from(service.refresh(uid(a), repo.getOrganization().getId(), id));
    }

    @DeleteMapping("/{id}/connect")
    public ResponseEntity<Void> disconnect(Authentication a, @PathVariable UUID id) {
        RepositoryConnection repo =
                repositories.findById(id).orElseThrow(() -> new NoSuchElementException("Repository not found"));
        service.disconnect(uid(a), repo.getOrganization().getId(), id);
        return ResponseEntity.noContent().build();
    }

    private UUID uid(Authentication a) {
        return UUID.fromString(a.getName());
    }

    public record Response(
            UUID id,
            UUID organizationId,
            String provider,
            String externalId,
            String name,
            String fullName,
            String cloneUrl,
            String defaultBranch,
            boolean privateRepository) {
        static Response from(RepositoryConnection r) {
            return new Response(
                    r.getId(),
                    r.getOrganization().getId(),
                    r.getProvider().name(),
                    r.getExternalId(),
                    r.getName(),
                    r.getFullName(),
                    r.getCloneUrl(),
                    r.getDefaultBranch(),
                    r.isPrivateRepo());
        }
    }
}
