package com.forgeci;

import com.forgeci.application.github.GitHubConnectionService;
import com.forgeci.application.pipeline.JobExecutor;
import com.forgeci.infrastructure.user.RefreshTokenRepository;
import com.forgeci.infrastructure.organization.OrganizationRepository;
import com.forgeci.infrastructure.organization.OrganizationMemberRepository;
import com.forgeci.infrastructure.repository.RepositoryConnectionRepository;
import com.forgeci.infrastructure.organization.GitHubConnectionRepository;
import com.forgeci.infrastructure.user.UserRepository;
import com.forgeci.infrastructure.webhook.WebhookDeliveryRepository;
import com.forgeci.infrastructure.pipeline.*;\nimport com.forgeci.infrastructure.secret.SecretRepository;\nimport com.forgeci.infrastructure.secret.RepositorySecretRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
})
class ForgeCiApplicationTests {
    @MockBean UserRepository userRepository;
    @MockBean RefreshTokenRepository refreshTokenRepository;
    @MockBean OrganizationRepository organizationRepository;
    @MockBean OrganizationMemberRepository organizationMemberRepository;
    @MockBean RepositoryConnectionRepository repositoryConnectionRepository;
    @MockBean GitHubConnectionRepository gitHubConnectionRepository;
    @MockBean WebhookDeliveryRepository webhookDeliveryRepository;
    @MockBean PipelineRunRepository pipelineRunRepository;
    @MockBean JobRunRepository jobRunRepository;
    @MockBean StepRunRepository stepRunRepository;
    @MockBean PipelineDispatchRepository pipelineDispatchRepository;
    @MockBean ProcessedMessageRepository processedMessageRepository;
    @MockBean LogChunkRepository logChunkRepository;
    @MockBean GitHubConnectionService gitHubConnectionService;
    @MockBean JobExecutor jobExecutor;
    @MockBean RabbitTemplate rabbitTemplate;\n    @MockBean SecretRepository secretRepository;\n    @MockBean RepositorySecretRepository repositorySecretRepository;

    @Test void contextLoads() {}
}
