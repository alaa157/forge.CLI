package com.forgeci.api;

import com.forgeci.domain.user.User;
import com.forgeci.infrastructure.user.UserRepository;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Phase 15 — formalize /users resource. */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserRepository users;

    public UserController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        UUID id = UUID.fromString(authentication.getName());
        User user = users.findById(id).orElseThrow(() -> new NoSuchElementException("User not found"));
        return UserResponse.from(user);
    }

    public record UserResponse(UUID id, String email, String displayName, String status, String role) {
        static UserResponse from(User u) {
            return new UserResponse(
                    u.getId(), u.getEmail(), u.getDisplayName(), u.getStatus().name(), u.getRole().name());
        }
    }
}
