package com.forgeci.domain.organization;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="github_connections",schema="forgeci")
public class GitHubConnection {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="organization_id",nullable=false) private Organization organization;
 @Column(nullable=false,length=30) private String provider="GITHUB";
 @Column(name="external_account_id",nullable=false,length=120) private String externalAccountId;
 @Column(name="account_login",nullable=false,length=120) private String accountLogin;
 @Column(name="encrypted_access_token",nullable=false,columnDefinition="text") private String encryptedAccessToken;
 @Column(name="token_expires_at") private Instant tokenExpiresAt;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private GitHubConnectionStatus status=GitHubConnectionStatus.ACTIVE;
 @Column(nullable=false,updatable=false) private Instant createdAt;
 @Column(nullable=false) private Instant updatedAt;
 private Instant disconnectedAt;
 protected GitHubConnection(){}
 public GitHubConnection(Organization org,String id,String login,String encryptedToken,Instant expires){organization=org;externalAccountId=id;accountLogin=login;encryptedAccessToken=encryptedToken;tokenExpiresAt=expires;}
 @PrePersist void create(){Instant n=Instant.now();createdAt=n;updatedAt=n;}
 @PreUpdate void update(){updatedAt=Instant.now();}
 public UUID getId(){return id;} public Organization getOrganization(){return organization;} public String getExternalAccountId(){return externalAccountId;} public String getAccountLogin(){return accountLogin;} public String getEncryptedAccessToken(){return encryptedAccessToken;} public Instant getTokenExpiresAt(){return tokenExpiresAt;} public GitHubConnectionStatus getStatus(){return status;}
 public void disconnect(){status=GitHubConnectionStatus.DISCONNECTED;disconnectedAt=Instant.now();encryptedAccessToken="";}
 public void markExpired(){status=GitHubConnectionStatus.EXPIRED;}
 public void updateToken(String encrypted,Instant expires){encryptedAccessToken=encrypted;tokenExpiresAt=expires;status=GitHubConnectionStatus.ACTIVE;disconnectedAt=null;}
}
