package com.forgeci.application.auth;
import com.forgeci.domain.user.User;
import com.forgeci.infrastructure.user.UserRepository;
import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class AuthService {
 private static final Pattern EMAIL=Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
 private final UserRepository users; private final PasswordEncoder passwords; private final PasswordPolicy policy; private final JwtService jwt; private final RefreshTokenService refresh;
 public AuthService(UserRepository users,PasswordEncoder passwords,PasswordPolicy policy,JwtService jwt,RefreshTokenService refresh){this.users=users;this.passwords=passwords;this.policy=policy;this.jwt=jwt;this.refresh=refresh;}
 @Transactional public User register(String email,String password,String displayName){
  String normalized=normalizeEmail(email); if(!EMAIL.matcher(normalized).matches()) throw new AuthException("INVALID_EMAIL","Invalid email address");
  policy.validate(password); if(displayName==null||displayName.isBlank()||displayName.length()>120) throw new AuthException("INVALID_DISPLAY_NAME","Display name is required");
  if(users.existsByEmail(normalized)) throw new AuthException("DUPLICATE_EMAIL","An account with this email already exists");
  return users.save(new User(normalized,passwords.encode(password),displayName.trim()));
 }
 @Transactional public Tokens login(String email,String password){
  User user=users.findByEmail(normalizeEmail(email)).orElseThrow(()->new AuthException("INVALID_CREDENTIALS","Invalid email or password"));
  if(user.getStatus()!=com.forgeci.domain.user.UserStatus.ACTIVE||!passwords.matches(password,user.getPasswordHash())) throw new AuthException("INVALID_CREDENTIALS","Invalid email or password");
  user.markLogin(Instant.now()); users.save(user); var r=refresh.issue(user); return new Tokens(jwt.issueAccessToken(user),r.token(),jwt.accessSeconds());
 }
 @Transactional public Tokens rotate(String refreshToken){var r=refresh.rotate(refreshToken);return new Tokens(jwt.issueAccessToken(r.user()),r.token(),jwt.accessSeconds());}
 static String normalizeEmail(String email){if(email==null)throw new AuthException("INVALID_EMAIL","Invalid email address");return email.trim().toLowerCase(Locale.ROOT);}
 public record Tokens(String accessToken,String refreshToken,long expiresIn){}
}
