package com.forgeci;

import com.forgeci.infrastructure.user.RefreshTokenRepository;
import com.forgeci.infrastructure.organization.OrganizationRepository;
import com.forgeci.infrastructure.organization.OrganizationMemberRepository;
import com.forgeci.infrastructure.repository.RepositoryRepository;
import com.forgeci.infrastructure.repository.GitHubConnectionRepository;
import com.forgeci.infrastructure.user.UserRepository;
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
    @MockBean RepositoryRepository repositoryRepository;
    @MockBean GitHubConnectionRepository gitHubConnectionRepository;

    @Test void contextLoads() {}
}
