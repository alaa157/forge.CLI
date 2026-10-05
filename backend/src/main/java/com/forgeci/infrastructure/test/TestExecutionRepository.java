package com.forgeci.infrastructure.test;
import com.forgeci.domain.test.TestExecution;import java.util.*;import org.springframework.data.jpa.repository.JpaRepository;
public interface TestExecutionRepository extends JpaRepository<TestExecution,UUID>{
 List<TestExecution> findTop1000ByTestIdOrderByExecutedAtDesc(String testId);
 List<TestExecution> findTop1000ByRepositoryIdAndTestIdOrderByExecutedAtDesc(UUID repositoryId,String testId);
 List<TestExecution> findTop1000ByRepositoryIdOrderByExecutedAtDesc(UUID repositoryId);
}