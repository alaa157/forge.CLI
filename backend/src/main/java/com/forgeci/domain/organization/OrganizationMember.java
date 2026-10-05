package com.forgeci.domain.organization;
import com.forgeci.domain.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="organization_members",schema="forgeci",uniqueConstraints=@UniqueConstraint(name="uk_org_member",columnNames={"organization_id","user_id"}))
public class OrganizationMember {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="organization_id",nullable=false) private Organization organization;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private OrganizationRole role;
 @Column(nullable=false,updatable=false) private Instant createdAt;
 protected OrganizationMember(){}
 public OrganizationMember(Organization o,User u,OrganizationRole r){organization=o;user=u;role=r;}
 @PrePersist void create(){createdAt=Instant.now();}
 public UUID getId(){return id;} public Organization getOrganization(){return organization;} public User getUser(){return user;} public OrganizationRole getRole(){return role;} public Instant getCreatedAt(){return createdAt;}
 public void changeRole(OrganizationRole role){this.role=role;}
}
