package com.forgeci.api;
import com.forgeci.application.auth.AuthService;
import jakarta.validation.Valid; import jakarta.validation.constraints.*;
import java.util.UUID;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/auth") public class AuthController {
 private final AuthService auth; public AuthController(AuthService auth){this.auth=auth;}
 @PostMapping("/register") ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest r){var u=auth.register(r.email(),r.password(),r.displayName());return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(u));}
 @PostMapping("/login") ResponseEntity<AuthService.Tokens> login(@Valid @RequestBody LoginRequest r){return ResponseEntity.ok(auth.login(r.email(),r.password()));}
 @PostMapping("/refresh") ResponseEntity<AuthService.Tokens> refresh(@Valid @RequestBody RefreshRequest r){return ResponseEntity.ok(auth.rotate(r.refreshToken()));}
 record RegisterRequest(@NotBlank @Email String email,@NotBlank String password,@NotBlank @Size(max=120) String displayName){}
 record LoginRequest(@NotBlank @Email String email,@NotBlank String password){}
 record RefreshRequest(@NotBlank String refreshToken){}
 record UserResponse(UUID id,String email,String displayName,String status,String role){static UserResponse from(com.forgeci.domain.user.User u){return new UserResponse(u.getId(),u.getEmail(),u.getDisplayName(),u.getStatus().name(),u.getRole().name());}}
}
