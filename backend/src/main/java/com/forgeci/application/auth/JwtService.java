package com.forgeci.application.auth;
import com.forgeci.domain.user.User;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Service
public class JwtService {
 private final JwtEncoder encoder; private final JwtDecoder decoder; private final long accessSeconds;
 public JwtService(@Value("\${forgeci.auth.jwt-secret}") String secret,@Value("\${forgeci.auth.access-token-seconds:900}") long accessSeconds) {
  if(secret.length()<32) throw new IllegalArgumentException("JWT secret must be at least 32 characters");
  var key=new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256");
  encoder=new NimbusJwtEncoder(new ImmutableSecret<>(key));
  decoder=NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build(); this.accessSeconds=accessSeconds;
 }
 public String issueAccessToken(User user){Instant now=Instant.now(); var claims=JwtClaimsSet.builder().issuer("forgeci").issuedAt(now).expiresAt(now.plus(accessSeconds,ChronoUnit.SECONDS)).subject(user.getId().toString()).claim("role",user.getRole().name()).build(); return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();}
 public JwtDecoder decoder(){return decoder;} public long accessSeconds(){return accessSeconds;}
}
