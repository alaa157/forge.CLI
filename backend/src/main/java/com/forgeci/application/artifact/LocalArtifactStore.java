package com.forgeci.application.artifact;
import com.forgeci.domain.artifact.StoredArtifact;
import java.io.*;import java.net.URI;import java.nio.file.*;import java.security.*;import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;import org.springframework.stereotype.Service;
@Service
public class LocalArtifactStore implements ArtifactStore{
 private final Path root;
 public LocalArtifactStore(@Value("${forgeci.artifacts.local-root:${java.io.tmpdir}/forgeci-artifacts}")String root){this.root=Path.of(root).toAbsolutePath().normalize();}
 public StoredArtifact put(String key,Path file,String type)throws IOException{
  Path target=safe(key);Files.createDirectories(target.getParent());Files.copy(file,target,StandardCopyOption.REPLACE_EXISTING);
  return new StoredArtifact(Files.size(target),sha256(target),type==null?"application/octet-stream":type);
 }
 public InputStream open(String key)throws IOException{return Files.newInputStream(safe(key),StandardOpenOption.READ);}
 public URI downloadUrl(String key,Duration ttl){return URI.create("/api/v1/artifacts/download?key="+java.net.URLEncoder.encode(key,java.nio.charset.StandardCharsets.UTF_8));}
 private Path safe(String key){
  if(key==null||key.isBlank()||key.contains("..")||key.startsWith("/")||key.startsWith("\\"))throw new IllegalArgumentException("Unsafe artifact key");
  return root.resolve(key).normalize().startsWith(root)?root.resolve(key).normalize():throwUnsafe();
 }
 private Path throwUnsafe(){throw new IllegalArgumentException("Unsafe artifact key");}
 private String sha256(Path p)throws IOException{try{MessageDigest d=MessageDigest.getInstance("SHA-256");try(InputStream in=Files.newInputStream(p)){byte[] b=new byte[8192];for(int n;(n=in.read(b))>0;)d.update(b,n);}return HexFormat.of().formatHex(d.digest());}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
}