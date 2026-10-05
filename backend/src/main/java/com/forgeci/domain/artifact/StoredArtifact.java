package com.forgeci.domain.artifact;
public record StoredArtifact(long sizeBytes,String sha256,String contentType) {}