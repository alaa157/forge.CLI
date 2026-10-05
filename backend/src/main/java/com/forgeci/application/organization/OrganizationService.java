package com.forgeci.application.organization;
import com.forgeci.domain.organization.*;
import com.forgeci.domain.user.User;
import com.forgeci.infrastructure.organization.*;
import com.forgeci.infrastructure.user.UserRepository;
import java.text.Normalizer;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class OrganizationService {
 private final OrganizationRepository organizations; private final OrganizationMemberRepository members; private final UserRepository users;
 public OrganizationService(OrganizationRepository o,OrganizationMemberRepository m,UserRepository u){organizations=o;members=m;users=u;}
 @Transactional public Organization create(UUID userId,String name,String requestedSlug){
  User user=users.findById(userId).orElseThrow(()->new OrganizationException("USER_NOT_FOUND","User not found"));
  if(name==null||name.isBlank()||name.trim().length()>120)throw new OrganizationException("INVALID_NAME","Organization name is required (max 120 characters)");
  String slug=slugify(requestedSlug==null||requestedSlug.isBlank()?name:requestedSlug);
  if(slug.length()>80||slug.isBlank())throw new OrganizationException("INVALID_SLUG","Organization slug is invalid");
  if(organizations.findBySlug(slug).isPresent())throw new OrganizationException("DUPLICATE_SLUG","Organization slug is already in use");
  Organization org=organizations.save(new Organization(name.trim(),slug,user));
  members.save(new OrganizationMember(org,user,OrganizationRole.OWNER));
  return org;
 }
 @Transactional(readOnly=true) public List<OrganizationMember> listForUser(UUID userId){return members.findAllByUserId(userId);}
 @Transactional(readOnly=true) public Organization requireMember(UUID orgId,UUID userId){return members.findByOrganizationIdAndUserId(orgId,userId).map(OrganizationMember::getOrganization).orElseThrow(()->new OrganizationException("ORGANIZATION_NOT_FOUND","Organization not found"));}
 @Transactional(readOnly=true) public Organization requireAdmin(UUID orgId,UUID userId){OrganizationMember m=members.findByOrganizationIdAndUserId(orgId,userId).orElseThrow(()->new OrganizationException("ORGANIZATION_NOT_FOUND","Organization not found"));if(m.getRole()==OrganizationRole.MEMBER)throw new OrganizationException("FORBIDDEN","Organization administrator permission required");return m.getOrganization();}
 @Transactional(readOnly=true) public List<OrganizationMember> listMembers(UUID orgId,UUID userId){requireMember(orgId,userId);return members.findAllByOrganizationId(orgId);}
 @Transactional public OrganizationMember addMember(UUID orgId,UUID actorId,String email,OrganizationRole role){
  Organization org=requireAdmin(orgId,actorId); User user=users.findByEmail(email==null?"":email.trim().toLowerCase(Locale.ROOT)).orElseThrow(()->new OrganizationException("USER_NOT_FOUND","User not found"));
  if(role==OrganizationRole.OWNER)throw new OrganizationException("INVALID_ROLE","Owner role is assigned only when an organization is created");
  if(members.existsByOrganizationIdAndUserId(orgId,user.getId()))throw new OrganizationException("ALREADY_MEMBER","User is already a member");
  return members.save(new OrganizationMember(org,user,role));
 }
 @Transactional public void changeRole(UUID orgId,UUID actorId,UUID memberId,OrganizationRole role){
  requireAdmin(orgId,actorId);OrganizationMember target=members.findById(memberId).filter(m->m.getOrganization().getId().equals(orgId)).orElseThrow(()->new OrganizationException("MEMBER_NOT_FOUND","Organization member not found"));
  if(target.getRole()==OrganizationRole.OWNER||role==OrganizationRole.OWNER)throw new OrganizationException("INVALID_ROLE","Owner transfer is not supported by this endpoint");
  target.changeRole(role);
 }
 @Transactional public void removeMember(UUID orgId,UUID actorId,UUID memberId){
  requireAdmin(orgId,actorId);OrganizationMember target=members.findById(memberId).filter(m->m.getOrganization().getId().equals(orgId)).orElseThrow(()->new OrganizationException("MEMBER_NOT_FOUND","Organization member not found"));
  if(target.getRole()==OrganizationRole.OWNER)throw new OrganizationException("OWNER_REQUIRED","Organization owner cannot be removed");
  members.delete(target);
 }
 private String slugify(String value){String n=Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT),Normalizer.Form.NFKD).replaceAll("\\p{M}","");return n.replaceAll("[^a-z0-9]+","-").replaceAll("(^-+|-+$)","");}
}
