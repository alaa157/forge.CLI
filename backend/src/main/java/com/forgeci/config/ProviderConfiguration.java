package com.forgeci.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
@Configuration public class ProviderConfiguration {
 @Bean RestClient githubRestClient(){return RestClient.builder().baseUrl("https://api.github.com").defaultHeader("Accept","application/vnd.github+json").defaultHeader("X-GitHub-Api-Version","2022-11-28").build();}
}
