package school.hei.demo.endpoint.rest.controller;

import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import school.hei.demo.endpoint.event.EventProducer;
import school.hei.demo.endpoint.event.model.ImageSubmitted;
import school.hei.demo.entity.ImageSubmission;
import school.hei.demo.file.bucket.BucketComponent;
import school.hei.demo.file.zip.FileTyper;
import school.hei.demo.repository.ImageSubmissionRepository;

@RestController
@RequestMapping("/images")
@AllArgsConstructor
public class ImageController {

  private final ImageSubmissionRepository imageSubmissionRepository;
  private final EventProducer<ImageSubmitted> eventProducer;
  private final BucketComponent bucketComponent;
  private final FileTyper fileTyper;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @SneakyThrows
  public ResponseEntity<ImageSubmission> submit(
      @RequestParam("file") MultipartFile file, @RequestParam("email") String email) {
    validateDeclaredContentType(file.getContentType());
    validateEmail(email);

    var tempFile = toTempFile(file);
    try {
      validateActualContentType(tempFile);

      var id = UUID.randomUUID();
      var fileName =
          file.getOriginalFilename() == null ? id.toString() : file.getOriginalFilename();
      var event = ImageSubmitted.builder().imageId(id).fileName(fileName).email(email).build();

      bucketComponent.upload(tempFile, event.originalBucketKey());

      var imageSubmission =
          imageSubmissionRepository.save(new ImageSubmission(id, fileName, email, Instant.now()));

      eventProducer.accept(List.of(event));

      return ResponseEntity.status(HttpStatus.CREATED).body(imageSubmission);
    } finally {
      Files.deleteIfExists(tempFile.toPath());
    }
  }

  @GetMapping
  public List<ImageSubmission> findAll() {
    return imageSubmissionRepository.findAll();
  }

  private void validateDeclaredContentType(String contentType) {
    var declared = contentType == null ? null : MediaType.parseMediaType(contentType);
    if (declared == null
        || !(declared.isCompatibleWith(MediaType.IMAGE_JPEG)
            || declared.isCompatibleWith(MediaType.IMAGE_PNG))) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Only image/jpeg and image/png files are accepted");
    }
  }

  private void validateActualContentType(File file) {
    var detected = fileTyper.apply(file);
    if (!(detected.isCompatibleWith(MediaType.IMAGE_JPEG)
        || detected.isCompatibleWith(MediaType.IMAGE_PNG))) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "File content does not match an accepted image type");
    }
  }

  private void validateEmail(String email) {
    try {
      new InternetAddress(email).validate();
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email address");
    }
  }

  @SneakyThrows
  private File toTempFile(MultipartFile file) {
    var originalFilename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
    var dotIndex = originalFilename.lastIndexOf('.');
    var extension = dotIndex >= 0 ? originalFilename.substring(dotIndex) : "";
    var tempFile = File.createTempFile("upload-", extension);
    file.transferTo(tempFile);
    return tempFile;
  }
}
