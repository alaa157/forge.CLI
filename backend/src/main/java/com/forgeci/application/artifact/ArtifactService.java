package com.forgeci.application.artifact;
import com.forgeci.domain.artifact.*;import com.forgeci.infrastructure.artifact.ArtifactRepository;
import java.io.*;import java.nio.file.Path;import java.time.Duration;import java.util.*;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;
@Service
public class ArtifactService{
 private final ArtifactRepository repository; private final ArtifactStore store;
 public ArtifactService(ArtifactRepository repository,ArtifactStore store){this.repository=repository;this.store=store;}
 @Transactional public Artifact save(UUID jobRunId,String name,Path file,String contentType)throws IOException{
  String key="jobs/"+jobRunId+"/"+UUID.randomUUID()+"/"+sanitize(name);
  StoredArtifact stored=store.put(key,file,contentType);
  return repository.save(new Artifact(jobRunId,sanitize(name),key,stored.sizeBytes(),stored.sha256(),stored.contentType()));
 }
 @Transactional(readOnly=true) public List<Artifact> list(UUID jobRunId){return repository.findAllByJobRunIdOrderByCreatedAtAsc(jobRunId);}
 @Transactional(readOnly=true) public Artifact get(UUID id){return repository.findById(id).orElseThrow(()->new NoSuchElementException("Artifact not found"));}
 public InputStream open(Artifact artifact)throws IOException{return store.open(artifact.getObjectKey());}
 public java.net.URI signedUrl(Artifact artifact){return store.downloadUrl(artifact.getObjectKey(),Duration.ofMinutes(10));}
 private String sanitize(String s){String n=Path.of(s).getFileName().toString();if(n.isBlank()||n.length()>255)throw new IllegalArgumentException("Invalid artifact name");return n;}
}