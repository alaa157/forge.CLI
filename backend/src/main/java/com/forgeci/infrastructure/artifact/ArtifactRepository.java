package com.forgeci.infrastructure.artifact;
import com.forgeci.domain.artifact.Artifact;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ArtifactRepository extends JpaRepository<Artifact,UUID>{
 List<Artifact> findAllByJobRunIdOrderByCreatedAtAsc(UUID jobRunId);
}