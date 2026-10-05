package com.forgeci.api;

import com.forgeci.application.secret.SecretService;
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
 private final SecretService service;
 public SecretController(SecretService s){service=s;}
 @PutMapping("/organizations/{organizationId}/{name}") public void putOrg(Authentication a,@PathVariable UUID organizationId,@PathVariable String name,@Valid @RequestBody ValueRequest r){service.putOrganizationSecret(organizationId,name,r.value());}
 @DeleteMapping("/organizations/{organizationId}/{name}") public void deleteOrg(Authentication a,@PathVariable UUID organizationId,@PathVariable String name){service.deleteOrganizationSecret(organizationId,name);}
 @GetMapping("/organizations/{organizationId}") public List<String> orgNames(Authentication a,@PathVariable UUID organizationId){return service.organizationNames(organizationId);}
 @PutMapping("/repositories/{repositoryId}/{name}") public void putRepo(Authentication a,@PathVariable UUID repositoryId,@PathVariable String name,@Valid @RequestBody ValueRequest r){service.putRepositorySecret(repositoryId,name,r.value());}
 @DeleteMapping("/repositories/{repositoryId}/{name}") public void deleteRepo(Authentication a,@PathVariable UUID repositoryId,@PathVariable String name){service.deleteRepositorySecret(repositoryId,name);}
 @GetMapping("/repositories/{repositoryId}") public List<String> repoNames(Authentication a,@PathVariable UUID repositoryId){return service.repositoryNames(repositoryId);}
 public record ValueRequest(@NotBlank @Size(max=8192) String value){}
}