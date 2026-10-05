package com.forgeci.application.auth;
import org.springframework.stereotype.Component;
@Component public class PasswordPolicy {
 public void validate(String p){
  if(p==null||p.length()<12||p.length()>128||p.chars().noneMatch(Character::isUpperCase)||p.chars().noneMatch(Character::isLowerCase)||p.chars().noneMatch(Character::isDigit))
   throw new AuthException("WEAK_PASSWORD","Password must be 12-128 characters and contain upper, lower, and numeric characters");
 }
}
