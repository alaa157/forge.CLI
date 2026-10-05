package com.forgeci.domain.organization;
import com.forgeci.domain.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="organizations",schema="forgeci")
public class Organization {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(nullable=false,length=120) private String name;
 @Column(nullable=false,length=80,unique=true) private String slug;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="created_by",nullable=false) private User createdBy;
 @Column(nullable=false,updatable=false) private Instant createdAt;
 @Column(nullable=false) private Instant updatedAt;
 protected Organization(){}
 public Organization(String name,String slug,User creator){this.name=name;this.slug=slug;this.createdBy=creator;}
 @PrePersist void create(){Instant now=Instant.now();createdAt=now;updatedAt=now;}
 @PreUpdate void update(){updatedAt=Instant.now();}
 public UUID getId(){return id;} public String getName(){return name;} public String getSlug(){return slug;} public User getCreatedBy(){return createdBy;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
