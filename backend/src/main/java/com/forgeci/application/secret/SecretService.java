package com.forgeci.application.secret;

import com.forgeci.application.security.EnvelopeSecretEncryptionService;
import com.forgeci.domain.secret.*;
import com.forgeci.infrastructure.secret.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecretService {
 private final SecretRepository orgSecrets; private final RepositorySecretRepository repoSecrets; private final EnvelopeSecretEncryptionService encryption;
 public SecretService(SecretRepository o,RepositorySecretRepository r,EnvelopeSecretEncryptionService e){orgSecrets=o;repoSecrets=r;encryption=e;}
 @Transactional public void putOrganizationSecret(UUID organizationId,String name,String value){validate(value);var existing=orgSecrets.findByOrganizationIdAndName(organizationId,name);if(existing.isPresent())existing.get().replaceCiphertext(encryption.encrypt(value));else orgSecrets.save(new Secret(organizationId,name,encryption.encrypt(value)));}
 @Transactional public void putRepositorySecret(UUID repositoryId,String name,String value){validate(value);var existing=repoSecrets.findByRepositoryIdAndName(repositoryId,name);if(existing.isPresent())existing.get().replaceCiphertext(encryption.encrypt(value));else repoSecrets.save(new RepositorySecret(repositoryId,name,encryption.encrypt(value)));}
 @Transactional public void deleteOrganizationSecret(UUID organizationId,String name){orgSecrets.findByOrganizationIdAndName(organizationId,name).ifPresent(orgSecrets::delete);}
 @Transactional public void deleteRepositorySecret(UUID repositoryId,String name){repoSecrets.findByRepositoryIdAndName(repositoryId,name).ifPresent(repoSecrets::delete);}
 @Transactional(readOnly=true) public List<String> organizationNames(UUID organizationId){return orgSecrets.findAllByOrganizationId(organizationId).stream().map(Secret::getName).sorted().toList();}
 @Transactional(readOnly=true) public List<String> repositoryNames(UUID repositoryId){return repoSecrets.findAllByRepositoryId(repositoryId).stream().map(RepositorySecret::getName).sorted().toList();}
 @Transactional(readOnly=true) public Map<String,String> resolve(UUID organizationId,UUID repositoryId,List<String> names){
  Map<String,String> result=new HashMap<>();
  for(String name:names){
   var local=repoSecrets.findByRepositoryIdAndName(repositoryId,name);
   var global=orgSecrets.findByOrganizationIdAndName(organizationId,name);
   if(local.isPresent()) result.put(name,encryption.decrypt(local.get().getCiphertext()));
   else if(global.isPresent()) result.put(name,encryption.decrypt(global.get().getCiphertext()));
  }
  return Map.copyOf(result);
 }
 private static void validate(String value){if(value==null||value.length()>8192)throw new IllegalArgumentException("Secret value is invalid");}
}