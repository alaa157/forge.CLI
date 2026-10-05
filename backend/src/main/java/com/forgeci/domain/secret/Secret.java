package com.forgeci.domain.secret;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="secrets",schema="forgeci",uniqueConstraints=@UniqueConstraint(name="uk_secret_org_name",columnNames={"organization_id","name"}))
public class Secret {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(name="organization_id",nullable=false) private UUID organizationId;
 @Column(nullable=false,length=128) private String name;
 @Column(name="ciphertext",nullable=false,columnDefinition="text") private String ciphertext;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @Column(name="updated_at",nullable=false) private Instant updatedAt;
 protected Secret(){}
 public Secret(UUID organizationId,String name,String ciphertext){if(organizationId==null)throw new IllegalArgumentException("organizationId is required");if(name==null||!name.matches("[A-Z_][A-Z0-9_]{0,127}"))throw new IllegalArgumentException("Invalid secret name");this.organizationId=organizationId;this.name=name;this.ciphertext=ciphertext;}
 @PrePersist void create(){Instant n=Instant.now();createdAt=n;updatedAt=n;} @PreUpdate void update(){updatedAt=Instant.now();}
 public UUID getId(){return id;} public UUID getOrganizationId(){return organizationId;} public String getName(){return name;} public String getCiphertext(){return ciphertext;}
 public void replaceCiphertext(String value){ciphertext=value;}
}