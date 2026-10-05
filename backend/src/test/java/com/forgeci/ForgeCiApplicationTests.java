package com.forgeci;

import com.forgeci.infrastructure.user.RefreshTokenRepository;
import com.forgeci.infrastructure.organization.OrganizationRepository;
import com.forgeci.infrastructure.organization.OrganizationMemberRepository;
import com.forgeci.infrastructure.repository.RepositoryConnectionRepository;
import com.forgeci.infrastructure.organization.GitHubConnectionRepository;
import com.forgeci.infrastructure.user.UserRepository;
import com.forgeci.infrastructure.webhook.WebhookDeliveryRepository;
import com.forgeci.infrastructure.pipeline.PipelineRunRepository;
import com.forgeci.infrastructure.pipeline.JobRunRepository;
import com.forgeci.infrastructure.pipeline.StepRunRepository;
import com.forgeci.infrastructure.pipeline.PipelineDispatchRepository;
import com.forgeci.infrastructure.pipeline.ProcessedMessageRepository;
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
    @MockBean RabbitTemplate rabbitTemplate;

    @Test void contextLoads() {}
}
