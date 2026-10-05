package com.forgeci.application.security;

import java.net.*;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class OutboundUrlPolicy {
 public void validate(URI uri){
  if(uri==null||uri.getScheme()==null||uri.getHost()==null)throw new IllegalArgumentException("Outbound URL is invalid");
  String scheme=uri.getScheme().toLowerCase(Locale.ROOT); if(!scheme.equals("https")&&!scheme.equals("http"))throw new IllegalArgumentException("Outbound URL scheme is not allowed");
  String host=uri.getHost().toLowerCase(Locale.ROOT);
  if(host.equals("localhost")||host.equals("metadata.google.internal")||host.equals("host.docker.internal")||isIpBlocked(host))throw new IllegalArgumentException("Outbound URL targets a blocked network");
  try{
   for(InetAddress a:InetAddress.getAllByName(host)){if(a.isAnyLocalAddress()||a.isLoopbackAddress()||a.isLinkLocalAddress()||a.isSiteLocalAddress()||isMetadata(a))throw new IllegalArgumentException("Outbound URL resolves to a blocked network");}
  }catch(UnknownHostException e){throw new IllegalArgumentException("Outbound URL host cannot be resolved");}
 }
 private boolean isIpBlocked(String host){try{return isBlocked(InetAddress.getByName(host));}catch(Exception e){return false;}}
 private boolean isBlocked(InetAddress a){return a.isAnyLocalAddress()||a.isLoopbackAddress()||a.isLinkLocalAddress()||a.isSiteLocalAddress()||isMetadata(a);}
 private boolean isMetadata(InetAddress a){byte[] b=a.getAddress();return b.length==4&&(b[0]&255)==169&&(b[1]&255)==254&&(b[2]&255)==169&&(b[3]&255)==254;}
}