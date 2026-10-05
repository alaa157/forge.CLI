package com.forgeci.api;

import com.forgeci.application.secret.SecretService;
import com.forgeci.application.organization.OrganizationService;
import com.forgeci.infrastructure.repository.RepositoryConnectionRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/secrets")
public class SecretController {
 private final SecretService service; private final OrganizationService organizations; private final RepositoryConnectionRepository repositories;
 public SecretController(SecretService s,OrganizationService o,RepositoryConnectionRepository r){service=s;organizations=o;repositories=r;}
 @PutMapping("/organizations/{organizationId}/{name}") public void putOrg(Authentication a,@PathVariable UUID organizationId,@PathVariable String name,@Valid @RequestBody ValueRequest r){requireOrg(a,organizationId); service.putOrganizationSecret(organizationId,name,r.value());}
 @DeleteMapping("/organizations/{organizationId}/{name}") public void deleteOrg(Authentication a,@PathVariable UUID organizationId,@PathVariable String name){requireOrg(a,organizationId); service.deleteOrganizationSecret(organizationId,name);}
 @GetMapping("/organizations/{organizationId}") public List<String> orgNames(Authentication a,@PathVariable UUID organizationId){requireOrg(a,organizationId); return service.organizationNames(organizationId);}
 @PutMapping("/repositories/{repositoryId}/{name}") public void putRepo(Authentication a,@PathVariable UUID repositoryId,@PathVariable String name,@Valid @RequestBody ValueRequest r){var repo=repositories.findById(repositoryId).orElseThrow(()->new IllegalArgumentException("Repository not found")); requireOrg(a,repo.getOrganization().getId()); service.putRepositorySecret(repositoryId,name,r.value());}
 @DeleteMapping("/repositories/{repositoryId}/{name}") public void deleteRepo(Authentication a,@PathVariable UUID repositoryId,@PathVariable String name){var repo=repositories.findById(repositoryId).orElseThrow(()->new IllegalArgumentException("Repository not found")); requireOrg(a,repo.getOrganization().getId()); service.deleteRepositorySecret(repositoryId,name);}
 @GetMapping("/repositories/{repositoryId}") public List<String> repoNames(Authentication a,@PathVariable UUID repositoryId){var repo=repositories.findById(repositoryId).orElseThrow(()->new IllegalArgumentException("Repository not found")); requireOrg(a,repo.getOrganization().getId()); return service.repositoryNames(repositoryId);}
 private void requireOrg(Authentication a,UUID id){if(!organizations.isMember(UUID.fromString(a.getName()),id))throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,"Not an organization member");}
 public record ValueRequest(@NotBlank @Size(max=8192) String value){}
}