package com.forgeci.application.webhook;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
class GitHubWebhookSignatureVerifierTest {
 @Test void verifiesGitHubDocumentedSignature(){
  var verifier=new GitHubWebhookSignatureVerifier("It's a Secret to Everybody");
  assertTrue(verifier.verify("Hello, World!".getBytes(java.nio.charset.StandardCharsets.UTF_8),"sha256=757107ea0eb2509fc211221cce984b8a37570b6d7586c22c46f4379c8b043e17"));
 }
 @Test void rejectsTamperedPayload(){
  var verifier=new GitHubWebhookSignatureVerifier("It's a Secret to Everybody");
  assertFalse(verifier.verify("Hello, ForgeCI!".getBytes(java.nio.charset.StandardCharsets.UTF_8),"sha256=757107b6d7586c22c46f4379c8b043e17"));
 }
 @Test void rejectsMissingOrMalformedSignature(){
  var verifier=new GitHubWebhookSignatureVerifier("secret");
  assertFalse(verifier.verify(new byte[]{1},null));
  assertFalse(verifier.verify(new byte[]{1},"sha1=abc"));
  assertFalse(verifier.verify(new byte[]{1},"sha256=not-hex"));
 }
}