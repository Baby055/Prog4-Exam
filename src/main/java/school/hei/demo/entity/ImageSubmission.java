package school.hei.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class ImageSubmission {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String fileName;
  private String email;
  private Instant createdAt;
}
