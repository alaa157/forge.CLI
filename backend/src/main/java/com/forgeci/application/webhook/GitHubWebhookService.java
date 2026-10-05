package com.forgeci.application.webhook;
import com.fasterxml.jackson.databind.JsonNode; import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgeci.application.github.GitHubClient; import com.forgeci.application.github.GitHubConnectionService; import com.forgeci.application.pipeline.PipelineConfiguration; import com.forgeci.application.pipeline.PipelineConfigurationService; import com.forgeci.application.pipeline.PipelineRunService;
import com.forgeci.domain.pipeline.PipelineRun; import com.forgeci.domain.repository.RepositoryConnection; import com.forgeci.domain.repository.RepositoryProvider; import com.forgeci.domain.webhook.WebhookDelivery; import com.forgeci.domain.webhook.WebhookDeliveryStatus;
import com.forgeci.infrastructure.repository.RepositoryConnectionRepository; import com.forgeci.infrastructure.webhook.WebhookDeliveryRepository;
import java.security.MessageDigest; import java.time.Instant; import java.util.HexFormat; import java.util.Optional; import java.util.UUID;
import org.slf4j.Logger; import org.slf4j.LoggerFactory; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service public class GitHubWebhookService {
 private static final Logger log=LoggerFactory.getLogger(GitHubWebhookService.class); private static final String PROVIDER="GITHUB"; private static final String PIPELINE_PATH=".forgeci.yml";
 private final WebhookDeliveryRepository deliveries; private final RepositoryConnectionRepository repositories; private final GitHubClient github; private final GitHubConnectionService connections; private final PipelineConfigurationService configurationService; private final PipelineRunService pipelineRuns; private final ObjectMapper mapper;
 public GitHubWebhookService(WebhookDeliveryRepository deliveries,RepositoryConnectionRepository repositories,GitHubClient github,GitHubConnectionService connections,PipelineConfigurationService configurationService,PipelineRunService pipelineRuns,ObjectMapper mapper){this.deliveries=deliveries;this.repositories=repositories;this.github=github;this.connections=connections;this.configurationService=configurationService;this.pipelineRuns=pipelineRuns;this.mapper=mapper;}
 @Transactional public Result process(String deliveryId,String eventType,byte[] payload){
  String hash=sha256(payload); UUID claimId=UUID.randomUUID();
  int claimed=deliveries.claim(claimId,PROVIDER,deliveryId,eventType,Instant.now(),hash);
  WebhookDelivery delivery=deliveries.findForUpdate(PROVIDER,deliveryId).orElseThrow(()->new IllegalStateException("Webhook delivery claim was lost"));
  if(claimed==0 && delivery.getStatus()==WebhookDeliveryStatus.PROCESSED)return Result.duplicate();
  try{
   if(!eventType.equals("push")&&!eventType.equals("pull_request")){delivery.markProcessed(Instant.now());return Result.ignored();}
   JsonNode root=mapper.readTree(payload); String externalId=text(root.at("/repository/id"));
   if(externalId==null)throw new WebhookException("INVALID_PAYLOAD","GitHub repository id is missing");
   Optional<RepositoryConnection> repo=repositories.findByProviderAndExternalId(RepositoryProvider.GITHUB,externalId);
   if(repo.isEmpty()){delivery.markProcessed(Instant.now());return Result.ignored();}
   EventData event=extract(eventType,root); if(event==null){delivery.markProcessed(Instant.now());return Result.ignored();}
   String token=connections.webhookToken(repo.get().getOrganization().getId()); Optional<String> yaml=github.repositoryFile(token,repo.get().getFullName(),PIPELINE_PATH,event.commitSha());
   if(yaml.isEmpty()){delivery.markProcessed(Instant.now());return Result.noPipeline();}
   PipelineConfiguration configuration=configurationService.load(yaml.get());
   PipelineRun run=pipelineRuns.create(repo.get().getId(),event.commitSha(),event.branch(),eventType,yaml.get(),configuration);
   delivery.markProcessed(Instant.now()); return Result.created(run.getId());
  }catch(Exception e){delivery.markFailed(Instant.now());log.error("GitHub webhook processing failed provider={} deliveryId={} eventType={}",PROVIDER,deliveryId,eventType,e);throw e instanceof RuntimeException ? (RuntimeException)e : new IllegalStateException("GitHub webhook processing failed",e);}
 }
 private EventData extract(String type,JsonNode root){
  if(type.equals("push")){String sha=text(root.at("/after"));String ref=text(root.at("/ref"));if(sha==null||ref==null||!ref.startsWith("refs/heads/")||isZero(sha))return null;return new EventData(sha,ref.substring("refs/heads/".length()));}
  String action=text(root.at("/action")); if(action==null||(!action.equals("opened")&&!action.equals("synchronize")&&!action.equals("reopened")&&!action.equals("ready_for_review")))return null;
  String sha=text(root.at("/pull_request/head/sha")); String branch=text(root.at("/pull_request/head/ref")); if(sha==null||branch==null||isZero(sha))return null; return new EventData(sha,branch);
 }
 private static boolean isZero(String value){return !value.isEmpty()&&value.chars().allMatch(c->c=='0');}
 private static String text(JsonNode node){return node==null||node.isMissingNode()||node.isNull()||node.asText().isBlank()?null:node.asText();}
 private static String sha256(byte[] payload){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(payload));}catch(Exception e){throw new IllegalStateException("Unable to hash webhook payload",e);}}
 private record EventData(String commitSha,String branch){}
 public record Result(Status status,UUID pipelineRunId){static Result created(UUID id){return new Result(Status.CREATED,id);}static Result duplicate(){return new Result(Status.DUPLICATE,null);}static Result ignored(){return new Result(Status.IGNORED,null);}static Result noPipeline(){return new Result(Status.NO_PIPELINE,null);}public enum Status{CREATED,DUPLICATE,IGNORED,NO_PIPELINE}}
 public static class WebhookException extends RuntimeException{private final String code;public WebhookException(String code,String message){super(message);this.code=code;}public String getCode(){return code;}}
}