package com.forgeci.application.artifact;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgeci.domain.artifact.StoredArtifact;
import java.io.*;import java.net.URI;import java.nio.file.*;import java.security.*;import java.time.Duration;
import io.minio.*;import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;import org.springframework.stereotype.Service;
@Service
@ConditionalOnProperty(name="forgeci.artifacts.backend",havingValue="s3")
public class S3ArtifactStore implements ArtifactStore{
 private final MinioClient client; private final String bucket;
 public S3ArtifactStore(@Value("${forgeci.artifacts.s3.endpoint:http://localhost:9000}")String endpoint,
  @Value("${forgeci.artifacts.s3.access-key:minioadmin}")String access,
  @Value("${forgeci.artifacts.s3.secret-key:minioadmin}")String secret,
  @Value("${forgeci.artifacts.s3.bucket:forgeci-artifacts}")String bucket){
  this.bucket=bucket;this.client=MinioClient.builder().endpoint(endpoint).credentials(access,secret).build();
  ensureBucket();
 }
 private void ensureBucket(){
  try{
   if(!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build()))
    client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
  }catch(Exception e){throw new IllegalStateException("Unable to ensure artifact bucket "+bucket,e);}
 }
 public StoredArtifact put(String key,Path file,String type)throws IOException{
  try{
   client.putObject(PutObjectArgs.builder().bucket(bucket).object(key).contentType(type==null?"application/octet-stream":type)
    .stream(Files.newInputStream(file),Files.size(file),-1).build());
   return new StoredArtifact(Files.size(file),sha256(file),type==null?"application/octet-stream":type);
  }catch(Exception e){throw new IOException("Unable to upload artifact",e);}
 }
 public InputStream open(String key)throws IOException{try{return client.getObject(GetObjectArgs.builder().bucket(bucket).object(key).build());}catch(Exception e){throw new IOException("Unable to open artifact",e);}}
 public URI downloadUrl(String key,Duration ttl){try{return URI.create(client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(bucket).object(key).expiry((int)ttl.toSeconds()).build()));}catch(Exception e){throw new IllegalStateException("Unable to create signed artifact URL",e);}}
 private String sha256(Path p)throws IOException{try{MessageDigest d=MessageDigest.getInstance("SHA-256");try(InputStream in=Files.newInputStream(p)){byte[] b=new byte[8192];for(int n;(n=in.read(b))>0;)d.update(b,n);}return HexFormat.of().formatHex(d.digest());}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
}
