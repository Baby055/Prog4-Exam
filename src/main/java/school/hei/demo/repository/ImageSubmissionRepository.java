package school.hei.demo.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.demo.entity.ImageSubmission;

@Repository
public interface ImageSubmissionRepository extends JpaRepository<ImageSubmission, UUID> {

    @Override
    List<ImageSubmission> findAll();
}