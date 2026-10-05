package com.forgeci.application.test;
import java.nio.charset.StandardCharsets;import java.security.MessageDigest;
public final class TestIdentity{
 private TestIdentity(){}
 public static String canonical(String framework,String className,String testName){return "forgeci::"+framework+"::"+className+"::"+testName;}
 public static String sha256(String canonical){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
}