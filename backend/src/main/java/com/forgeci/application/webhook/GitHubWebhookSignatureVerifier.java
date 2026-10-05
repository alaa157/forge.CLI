package com.forgeci.application.webhook;
import java.nio.charset.StandardCharsets; import java.security.MessageDigest; import java.util.HexFormat; import javax.crypto.Mac; import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Component;
@Component public class GitHubWebhookSignatureVerifier{
 private final byte[] secret;
 public GitHubWebhookSignatureVerifier(@Value("${forgeci.github.webhook-secret:}") String secret){this.secret=secret.getBytes(StandardCharsets.UTF_8);}
 public boolean isConfigured(){return secret.length>0;}
 public boolean verify(byte[] payload,String signatureHeader){
  if(!isConfigured()||payload==null||signatureHeader==null||!signatureHeader.startsWith("sha256="))return false;
  String hex=signatureHeader.substring(7); if(hex.length()!=64)return false; final byte[] supplied;
  try{supplied=HexFormat.of().parseHex(hex);}catch(IllegalArgumentException e){return false;}
  try{Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret,"HmacSHA256"));return MessageDigest.isEqual(mac.doFinal(payload),supplied);}
  catch(Exception e){throw new IllegalStateException("Unable to verify GitHub webhook signature",e);}
 }
}