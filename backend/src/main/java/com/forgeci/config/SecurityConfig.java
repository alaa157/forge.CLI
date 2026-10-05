package com.forgeci.config;
import com.forgeci.application.auth.JwtService;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
@Configuration @EnableMethodSecurity public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();}
 @Bean SecurityFilterChain securityFilterChain(HttpSecurity http,JwtService jwt) throws Exception{
  var converter=new JwtAuthenticationConverter();
  converter.setJwtGrantedAuthoritiesConverter(token->java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_"+token.getClaimAsString("role"))));
  http.csrf(c->c.disable()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a->a.requestMatchers("/api/v1/auth/**","/api/v1/organizations/*/connections/github/callback","/api/v1/health","/actuator/health","/actuator/prometheus").permitAll().anyRequest().authenticated())
   .oauth2ResourceServer(o->o.jwt(j->j.decoder(jwt.decoder()).jwtAuthenticationConverter(converter)));
  return http.build();
 }
}
