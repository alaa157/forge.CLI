package com.forgeci.application.security;

import java.util.*;
import java.util.regex.Pattern;

public final class SecretMasker {
 private SecretMasker(){}
 public static String mask(String text,Collection<String> secrets){
  if(text==null||text.isEmpty()||secrets==null||secrets.isEmpty())return text;
  String out=text;
  for(String secret:secrets) if(secret!=null&&!secret.isBlank()&&secret.length()>=3) out=out.replace(secret,"********");
  return out;
 }
}