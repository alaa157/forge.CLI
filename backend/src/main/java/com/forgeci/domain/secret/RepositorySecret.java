package com.forgeci.domain.secret;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="repository_secrets",schema="forgeci",uniqueConstraints=@UniqueConstraint(name="uk_repo_secret_name",columnNames={"repository_id","name"}))
public class RepositorySecret {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(name="repository_id",nullable=false) private UUID repositoryId;
 @Column(nullable=false,length=128) private String name;
 @Column(name="ciphertext",nullable=false,columnDefinition="text") private String ciphertext;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @Column(name="updated_at",nullable=false) private Instant updatedAt;
 protected RepositorySecret(){}
 public RepositorySecret(UUID repositoryId,String name,String ciphertext){if(repositoryId==null)throw new IllegalArgumentException("repositoryId is required");if(name==null||!name.matches("[A-Z_][A-Z0-9_]{0,127}"))throw new IllegalArgumentException("Invalid secret name");this.repositoryId=repositoryId;this.name=name;this.ciphertext=ciphertext;}
 @PrePersist void create(){Instant n=Instant.now();createdAt=n;updatedAt=n;} @PreUpdate void update(){updatedAt=Instant.now();}
 public UUID getId(){return id;} public UUID getRepositoryId(){return repositoryId;} public String getName(){return name;} public String getCiphertext(){return ciphertext;}
 public void replaceCiphertext(String value){ciphertext=value;}
}