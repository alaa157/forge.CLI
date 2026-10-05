package com.forgeci.application.auth;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PasswordPolicyTest {
 private final PasswordPolicy policy=new PasswordPolicy();
 @Test void acceptsStrongPassword(){assertDoesNotThrow(()->policy.validate("StrongPassword1!"));}
 @Test void rejectsWeakPassword(){assertThrows(AuthException.class,()->policy.validate("password"));}
}
