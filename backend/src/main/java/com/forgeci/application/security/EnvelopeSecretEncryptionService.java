package com.forgeci.application.security;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EnvelopeSecretEncryptionService {
    private static final int IV_BYTES = 12;
    private static final int KEY_BITS = 256;
    private final SecretKeySpec masterKey;
    private final SecureRandom random = new SecureRandom();

    public EnvelopeSecretEncryptionService(@Value("${forgeci.security.encryption-key}") String encoded) {
        byte[] key;
        try { key = Base64.getDecoder().decode(encoded); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Encryption key must be base64", e); }
        if (key.length != 32) throw new IllegalArgumentException("Encryption key must decode to 32 bytes");
        masterKey = new SecretKeySpec(key, "AES");
    }

    public String encrypt(String value) {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(KEY_BITS, random);
            byte[] dataKey = generator.generateKey().getEncoded();
            byte[] wrapped = encrypt(masterKey, dataKey);
            byte[] payload = encrypt(new SecretKeySpec(dataKey, "AES"), value.getBytes(StandardCharsets.UTF_8));
            return "v1." + Base64.getEncoder().encodeToString(wrapped) + "." + Base64.getEncoder().encodeToString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to encrypt secret", e);
        }
    }

    public String decrypt(String encoded) {
        try {
            String[] parts = encoded.split("\\.", -1);
            if (parts.length != 3 || !"v1".equals(parts[0])) throw new IllegalArgumentException("Unsupported secret ciphertext");
            byte[] dataKey = decrypt(masterKey, Base64.getDecoder().decode(parts[1]));
            byte[] value = decrypt(new SecretKeySpec(dataKey, "AES"), Base64.getDecoder().decode(parts[2]));
            return new String(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to decrypt secret", e);
        }
    }

    private byte[] encrypt(SecretKeySpec key, byte[] plaintext) throws Exception {
        byte[] iv = new byte[IV_BYTES];
        random.nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        byte[] ciphertext = cipher.doFinal(plaintext);
        byte[] out = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, out, 0, iv.length);
        System.arraycopy(ciphertext, 0, out, iv.length, ciphertext.length);
        return out;
    }

    private byte[] decrypt(SecretKeySpec key, byte[] encoded) throws Exception {
        if (encoded.length <= IV_BYTES) throw new IllegalArgumentException("Invalid encrypted value");
        byte[] iv = java.util.Arrays.copyOfRange(encoded, 0, IV_BYTES);
        byte[] ciphertext = java.util.Arrays.copyOfRange(encoded, IV_BYTES, encoded.length);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
        return cipher.doFinal(ciphertext);
    }
}
