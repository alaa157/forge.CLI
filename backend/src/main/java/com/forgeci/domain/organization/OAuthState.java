package com.forgeci.domain.organization;
import com.forgeci.domain.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="oauth_states",schema="forgeci")
public class OAuthState {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="organization_id",nullable=false) private Organization organization;
 @Column(name="state_hash",nullable=false,length=64,unique=true) private String stateHash;
 @Column(name="expires_at",nullable=false) private Instant expiresAt;
 @Column(name="consumed_at") private Instant consumedAt;
 @Column(nullable=false,updatable=false) private Instant createdAt;
 protected OAuthState(){}
 public OAuthState(User user,Organization organization,String stateHash,Instant expiresAt){this.user=user;this.organization=organization;this.stateHash=stateHash;this.expiresAt=expiresAt;}
 @PrePersist void create(){createdAt=Instant.now();}
 public User getUser(){return user;} public Organization getOrganization(){return organization;}
 public boolean consume(Instant now){if(consumedAt!=null||!expiresAt.isAfter(now))return false;consumedAt=now;return true;}
}
