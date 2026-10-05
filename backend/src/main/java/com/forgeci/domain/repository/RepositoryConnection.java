package com.forgeci.domain.repository;
import com.forgeci.domain.organization.Organization;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="repositories",schema="forgeci",uniqueConstraints=@UniqueConstraint(name="uk_repository_external",columnNames={"organization_id","provider","external_id"}))
public class RepositoryConnection {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="organization_id",nullable=false) private Organization organization;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private RepositoryProvider provider;
 @Column(name="external_id",nullable=false,length=120) private String externalId;
 @Column(nullable=false,length=200) private String name;
 @Column(name="full_name",nullable=false,length=400) private String fullName;
 @Column(name="clone_url",nullable=false,columnDefinition="text") private String cloneUrl;
 @Column(name="default_branch") private String defaultBranch;
 @Column(name="private",nullable=false) private boolean privateRepo;
 @Column(nullable=false,updatable=false) private Instant createdAt;
 @Column(nullable=false) private Instant updatedAt;
 protected RepositoryConnection(){}
 public RepositoryConnection(Organization o,String externalId,String name,String fullName,String cloneUrl,String branch,boolean privateRepo){organization=o;provider=RepositoryProvider.GITHUB;this.externalId=externalId;this.name=name;this.fullName=fullName;this.cloneUrl=cloneUrl;defaultBranch=branch;this.privateRepo=privateRepo;}
 @PrePersist void create(){Instant now=Instant.now();createdAt=now;updatedAt=now;}
 @PreUpdate void update(){updatedAt=Instant.now();}
 public UUID getId(){return id;} public Organization getOrganization(){return organization;} public RepositoryProvider getProvider(){return provider;} public String getExternalId(){return externalId;} public String getName(){return name;} public String getFullName(){return fullName;} public String getCloneUrl(){return cloneUrl;} public String getDefaultBranch(){return defaultBranch;} public boolean isPrivateRepo(){return privateRepo;}
 public void refresh(String name,String fullName,String cloneUrl,String branch,boolean isPrivate){this.name=name;this.fullName=fullName;this.cloneUrl=cloneUrl;this.defaultBranch=branch;this.privateRepo=isPrivate;}
}
