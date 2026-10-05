package com.forgeci.infrastructure.repository;
import com.forgeci.domain.repository.GitHubConnection; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface GitHubConnectionRepository extends JpaRepository<GitHubConnection,UUID>{Optional<GitHubConnection> findByUserId(UUID userId);}
