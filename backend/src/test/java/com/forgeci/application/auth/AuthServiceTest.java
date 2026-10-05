package com.forgeci.application.auth;
import com.forgeci.domain.user.User;
import com.forgeci.infrastructure.user.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
class AuthServiceTest {
 private UserRepository users; private AuthService service;
 @BeforeEach void setUp(){
  users=Mockito.mock(UserRepository.class);
  service=new AuthService(users,Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8(),new PasswordPolicy(),Mockito.mock(JwtService.class),Mockito.mock(RefreshTokenService.class));
 }
 @Test void registrationNormalizesEmailAndDoesNotExposeHash(){
  Mockito.when(users.existsByEmail("user@example.com")).thenReturn(false);
  Mockito.when(users.save(Mockito.any(User.class))).thenAnswer(i->i.getArgument(0));
  User user=service.register(" USER@Example.COM ","StrongPassword1!","Alice");
  assertEquals("user@example.com",user.getEmail());
  assertNotEquals("StrongPassword1!",user.getPasswordHash());
  assertTrue(user.getPasswordHash().startsWith("$argon2"));
 }
 @Test void duplicateEmailIsRejected(){
  Mockito.when(users.existsByEmail("user@example.com")).thenReturn(true);
  assertThrows(AuthException.class,()->service.register("user@example.com","StrongPassword1!","Alice"));
  Mockito.verify(users,Mockito.never()).save(Mockito.any());
 }
}
