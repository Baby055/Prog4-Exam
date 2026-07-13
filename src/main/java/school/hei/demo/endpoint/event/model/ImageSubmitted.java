package school.hei.demo.endpoint.event.model;

import java.time.Duration;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class ImageSubmitted extends PojaEvent {
  private UUID imageId;
  private String fileName;
  private String email;

  private static final String ORIGINAL_PREFIX = "images/original/";
  private static final String GRAYSCALE_PREFIX = "images/bw/";

  public String originalBucketKey() {
    return ORIGINAL_PREFIX + imageId + "-" + fileName;
  }

  public String grayscaleBucketKey() {
    return GRAYSCALE_PREFIX + imageId + "-" + fileName;
  }

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(60);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(30);
  }
}
