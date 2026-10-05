package com.forgeci.api;
import com.forgeci.application.github.GitHubConnectionService;
import java.net.URI; import java.util.UUID;
import org.springframework.http.*; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/organizations/{organizationId}/connections/github")
public class GitHubConnectionController {
 private final GitHubConnectionService service; public GitHubConnectionController(GitHubConnectionService s){service=s;}
 @GetMapping("/authorize") ResponseEntity<Void> authorize(Authentication a,@PathVariable UUID organizationId){return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(service.authorizeUrl(uid(a),organizationId))).build();}
 @GetMapping("/callback") ResponseEntity<Void> callback(@RequestParam String state,@RequestParam String code)throws Exception{service.callback(state,code);return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/settings?github=connected")).build();}
 @DeleteMapping ResponseEntity<Void> disconnect(Authentication a,@PathVariable UUID organizationId){service.disconnect(uid(a),organizationId);return ResponseEntity.noContent().build();}
 private UUID uid(Authentication a){return UUID.fromString(a.getName());}
}
