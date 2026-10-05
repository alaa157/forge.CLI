package com.forgeci.api.artifact;
import com.forgeci.application.artifact.ArtifactService;import com.forgeci.domain.artifact.Artifact;
import java.io.*;import java.net.URI;import java.util.List;import java.util.UUID;
import org.springframework.core.io.InputStreamResource;import org.springframework.http.*;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1")
public class ArtifactController{
 private final ArtifactService service;
 public ArtifactController(ArtifactService service){this.service=service;}
 @GetMapping("/jobs/{id}/artifacts") public List<Artifact> list(@PathVariable UUID id){return service.list(id);}
 @GetMapping("/artifacts/{id}/download") public ResponseEntity<?> download(@PathVariable UUID id)throws IOException{
  Artifact a=service.list(id).stream().filter(x->x.getId().equals(id)).findFirst().orElse(null);
  if(a==null)return ResponseEntity.notFound().build();
  URI url=service.signedUrl(a);
  if(url.isAbsolute())return ResponseEntity.status(HttpStatus.FOUND).location(url).build();
  return ResponseEntity.ok().contentType(MediaType.parseMediaType(a.getContentType())).contentLength(a.getSizeBytes())
   .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+a.getName().replace("\"","_")+"\"")
   .body(new InputStreamResource(service.open(a)));
 }
}