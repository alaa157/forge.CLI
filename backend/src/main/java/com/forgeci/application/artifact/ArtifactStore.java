package com.forgeci.application.artifact;
import com.forgeci.domain.artifact.StoredArtifact;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
public interface ArtifactStore {
 StoredArtifact put(String objectKey,Path file,String contentType)throws IOException;
 InputStream open(String objectKey)throws IOException;
 URI downloadUrl(String objectKey,Duration ttl);
}