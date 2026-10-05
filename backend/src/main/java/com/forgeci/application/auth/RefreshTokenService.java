package com.forgeci.application.auth;
import com.forgeci.domain.user.RefreshToken;
import com.forgeci.domain.user.User;
import com.forgeci.infrastructure.user.RefreshTokenRepository;
import java.nio.charset.StandardCharsets; import java.security.MessageDigest; import java.security.SecureRandom; import java.time.Duration; import java.time.Instant; import java.util.Base64; import java.util.UUID;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service public class RefreshTokenService {
 private final RefreshTokenRepository repository; private final Duration lifetime; private final SecureRandom random=new SecureRandom();
 public RefreshTokenService(RefreshTokenRepository repository,@Value("\${forgeci.auth.refresh-token-days:30}") long days){this.repository=repository;this.lifetime=Duration.ofDays(days);}
 @Transactional public IssuedRefreshToken issue(User user){String raw=randomToken();UUID family=UUID.randomUUID();repository.save(new RefreshToken(user,hash(raw),family,Instant.now().plus(lifetime)));return new IssuedRefreshToken(raw,family);}
 @Transactional public RotationResult rotate(String rawToken){RefreshToken current=repository.findByTokenHash(hash(rawToken)).orElseThrow(()->new AuthException("INVALID_REFRESH_TOKEN","Invalid refresh token"));Instant now=Instant.now();if(!current.isUsable(now)){repository.revokeFamily(current.getFamilyId());throw new AuthException("REFRESH_TOKEN_REUSE","Refresh token is no longer valid");}current.markUsed(now);repository.save(current);repository.revokeFamily(current.getFamilyId());String raw=randomToken();repository.save(new RefreshToken(current.getUser(),hash(raw),current.getFamilyId(),now.plus(lifetime)));return new RotationResult(current.getUser(),raw);}
 private String randomToken(){byte[] bytes=new byte[48];random.nextBytes(bytes);return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);}
 static String hash(String raw){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
 public record IssuedRefreshToken(String token,UUID familyId){} public record RotationResult(User user,String token){}
}
