import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import javax.imageio.ImageIO;

public class ImageUtils {
	public static String fileToBase64(String folder, String imageName) {
		String base64Image;
		try {
			File image = new File(getImagePath(folder, imageName));
			byte[] bytes = new byte[(int) image.length()];
			FileInputStream fis = new FileInputStream(image);
			fis.read(bytes);
			fis.close();
			base64Image = Base64.getEncoder().encodeToString(bytes);
		} catch (Exception e) {
			System.out.println("Could not read image. Please insert a valid folder and image name.");
			return null;
		}
		return base64Image;
	}

	public static void base64ToFile(String folder, String fileName, String encodedImage, String extension) throws Exception {
		byte[] decodedBytes = java.util.Base64.getDecoder().decode(encodedImage);
		ByteArrayInputStream bais = new ByteArrayInputStream(decodedBytes);
		BufferedImage bufferedImage = ImageIO.read(bais);
		String fileNameExt = fileName + "." + extension;
		File output = new File(folder, fileNameExt);
		ImageIO.write(bufferedImage, extension, output);
	}

	public static String getImagePath(String folder, String imageName) {
		Path basePath = Paths.get(new File("").getAbsolutePath());
		Path imagePath = Paths.get(imageName);
		Path fullPath = basePath.resolve(folder).resolve(imagePath);
		return fullPath.toString();
	}
}
