package school.hei.demo.file.image;

import static java.io.File.createTempFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Function;
import javax.imageio.ImageIO;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
public class GrayscaleImageConverter implements Function<File, File> {

  @SneakyThrows
  @Override
  public File apply(File source) {
    var original = ImageIO.read(source);
    if (original == null) {
      throw new IllegalArgumentException("Unreadable image file: " + source.getName());
    }

    var grayscale =
        new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
    var graphics = grayscale.createGraphics();
    try {
      graphics.drawImage(original, 0, 0, null);
    } finally {
      graphics.dispose();
    }

    var format = formatOf(source);
    var destination = createTempFile("grayscale-", "." + format);
    ImageIO.write(grayscale, format, destination);
    return destination;
  }

  private String formatOf(File source) {
    var name = source.getName();
    var dotIndex = name.lastIndexOf('.');
    var extension = dotIndex >= 0 ? name.substring(dotIndex + 1).toLowerCase() : "";
    return switch (extension) {
      case "jpg", "jpeg" -> "jpg";
      default -> "png";
    };
  }
}
