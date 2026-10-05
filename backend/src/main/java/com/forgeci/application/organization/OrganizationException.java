package com.forgeci.application.organization;
public class OrganizationException extends RuntimeException { private final String code; public OrganizationException(String c,String m){super(m);code=c;} public String getCode(){return code;} }
