package com.forgeci.application.security;
import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
class FieldEncryptionServiceTest { @Test void roundTripsWithoutStoringPlaintext(){var s=new FieldEncryptionService("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");var c=s.encrypt("github-secret");assertNotEquals("github-secret",c);assertEquals("github-secret",s.decrypt(c));} }
