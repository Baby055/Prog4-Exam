package school.hei.demo.service.event;

import jakarta.mail.internet.InternetAddress;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.demo.endpoint.event.model.ImageSubmitted;
import school.hei.demo.file.bucket.BucketComponent;
import school.hei.demo.file.image.GrayscaleImageConverter;
import school.hei.demo.mail.Email;
import school.hei.demo.mail.Mailer;

@Service
@AllArgsConstructor
@Slf4j
public class ImageSubmittedService implements Consumer<ImageSubmitted> {

  private static final Duration DOWNLOAD_LINK_EXPIRATION = Duration.ofDays(7);

  private final BucketComponent bucketComponent;
  private final GrayscaleImageConverter grayscaleImageConverter;
  private final Mailer mailer;

  @SneakyThrows
  @Override
  public void accept(ImageSubmitted imageSubmitted) {
    var original = bucketComponent.download(imageSubmitted.originalBucketKey());
    var grayscale = grayscaleImageConverter.apply(original);

    var grayscaleBucketKey = imageSubmitted.grayscaleBucketKey();
    bucketComponent.upload(grayscale, grayscaleBucketKey);

    var downloadLink = bucketComponent.presign(grayscaleBucketKey, DOWNLOAD_LINK_EXPIRATION);

    var recipient = new InternetAddress(imageSubmitted.getEmail());
    var subject = "Votre image en noir et blanc est prête";
    var body =
        "Bonjour,<br/><br/>Votre image \""
            + imageSubmitted.getFileName()
            + "\" a été convertie en noir et blanc.<br/>"
            + "Vous pouvez la télécharger via ce lien (valable 7 jours) : "
            + "<a href=\""
            + downloadLink
            + "\">"
            + downloadLink
            + "</a>";

    mailer.accept(new Email(recipient, List.of(), List.of(), subject, body, List.of()));
    log.info(
        "Image {} converted to grayscale and email sent to {}",
        imageSubmitted.getImageId(),
        imageSubmitted.getEmail());
  }
}
