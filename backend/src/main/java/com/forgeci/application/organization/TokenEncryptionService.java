package com.forgeci.application.organization;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
@Service public class TokenEncryptionService {
 private final SecretKeySpec key; private final SecureRandom random=new SecureRandom();
 public TokenEncryptionService(@Value("${forgeci.security.encryption-key}") String encodedKey){
  if(encodedKey==null||encodedKey.isBlank())throw new IllegalStateException("forgeci.security.encryption-key must be configured with a base64-encoded 32-byte key");
  byte[] bytes;try{bytes=Base64.getDecoder().decode(encodedKey);}catch(IllegalArgumentException e){throw new IllegalStateException("forgeci.security.encryption-key must be valid base64",e);}
  if(bytes.length!=32)throw new IllegalStateException("forgeci.security.encryption-key must decode to 32 bytes");key=new SecretKeySpec(bytes,"AES");
 }
 public String encrypt(String value){try{byte[] iv=new byte[12];random.nextBytes(iv);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key,new GCMParameterSpec(128,iv));byte[] ciphertext=c.doFinal(value.getBytes(StandardCharsets.UTF_8));ByteBuffer b=ByteBuffer.allocate(iv.length+ciphertext.length);b.put(iv).put(ciphertext);return Base64.getEncoder().encodeToString(b.array());}catch(Exception e){throw new IllegalStateException("Could not encrypt provider credential",e);}}
 public String decrypt(String value){try{byte[] all=Base64.getDecoder().decode(value);ByteBuffer b=ByteBuffer.wrap(all);byte[] iv=new byte[12];b.get(iv);byte[] ciphertext=new byte[b.remaining()];b.get(ciphertext);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key,new GCMParameterSpec(128,iv));return new String(c.doFinal(ciphertext),StandardCharsets.UTF_8);}catch(Exception e){throw new IllegalStateException("Could not decrypt provider credential",e);}}
}
