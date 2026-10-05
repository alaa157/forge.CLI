package com.forgeci.api;

import com.forgeci.application.organization.OrganizationService;
import com.forgeci.domain.organization.Organization;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {
    private final OrganizationService service;

    public OrganizationController(OrganizationService s) {
        service = s;
    }

    @GetMapping
    public List<Response> list(Authentication a) {
        return service.listForUser(UUID.fromString(a.getName())).stream().map(Response::from).toList();
    }

    @PostMapping
    public ResponseEntity<Response> create(Authentication a, @Valid @RequestBody Request r) {
        var o = service.create(UUID.fromString(a.getName()), r.name(), r.slug());
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.from(o));
    }

    public record Request(
            @NotBlank @Size(max = 120) String name, @NotBlank @Size(max = 80) String slug) {}

    public record Response(UUID id, String name, String slug) {
        static Response from(Organization o) {
            return new Response(o.getId(), o.getName(), o.getSlug());
        }
    }
}
