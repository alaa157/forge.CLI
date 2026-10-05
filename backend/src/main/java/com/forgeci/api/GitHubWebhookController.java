package com.forgeci.api;
import com.forgeci.application.webhook.GitHubWebhookService;
import com.forgeci.application.webhook.GitHubWebhookSignatureVerifier;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/webhooks/github") public class GitHubWebhookController{
 private final GitHubWebhookService service; private final GitHubWebhookSignatureVerifier verifier;
 public GitHubWebhookController(GitHubWebhookService service,GitHubWebhookSignatureVerifier verifier){this.service=service;this.verifier=verifier;}
 @PostMapping(consumes=MediaType.APPLICATION_JSON_VALUE) public ResponseEntity<Response> receive(@RequestHeader(value="X-GitHub-Event",required=false)String eventType,@RequestHeader(value="X-GitHub-Delivery",required=false)String deliveryId,@RequestHeader(value="X-Hub-Signature-256",required=false)String signature,@RequestBody byte[] payload){
  if(!verifier.isConfigured()||!verifier.verify(payload,signature))return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  if(eventType==null||eventType.isBlank()||deliveryId==null||deliveryId.isBlank())return ResponseEntity.badRequest().build();
  GitHubWebhookService.Result result=service.process(deliveryId,eventType,payload);
  return ResponseEntity.status(HttpStatus.ACCEPTED).body(new Response(result.status().name(),result.pipelineRunId()));
 }
 record Response(String status,java.util.UUID pipelineRunId){}
}
