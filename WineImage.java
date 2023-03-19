import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public class WineImage {
	public static File createFolder() {
		// Create a folder for the images
		File folder = new File("server-images");
		if (!folder.exists()) {
			folder.mkdir();
			System.out.println("Created folder: server-images");
		}
		return folder;
	}

/* 	private static String writeImageToFile(File folder, BufferedImage image, String extension) throws IOException {
		// String fileName = "server-image-" + System.currentTimeMillis() + "." +
		// extension;
		String fileName = "portao.jpg";
		File output = new File(folder, fileName);
		ImageIO.write(image, extension, output);
		return fileName;
	} */

/* 	// Helper method to get the file extension of an image
	private static String getImageFileExtension(BufferedImage image) {
		if (image == null) {
			return "";
		}
		String extension = "";
		switch (image.getType()) {
			case BufferedImage.TYPE_3BYTE_BGR:
			case BufferedImage.TYPE_4BYTE_ABGR:
				extension = "png";
				break;
			default:
				extension = "jpg";
				break;
		}
		return extension;
	} */

	public static BufferedImage readImageFromNetwork(ObjectInputStream in) throws Exception {
		return ImageIO.read(new ByteArrayInputStream((byte[]) in.readObject()));
	}

/* 	public static String getImagePath(String ImageName) {
		Path basePath = Paths.get(new File("").getAbsolutePath());
		Path imagePath = Paths.get(ImageName);
		Path fullPath = basePath.resolve(imagePath);
		return fullPath.toString();
	} */

	public static boolean sendImage(BufferedImage image, ObjectOutputStream out) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, getImageFileExtension(image), baos);
			out.writeObject(baos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Sent image to server.");
		return true;
	}

/* 	public static BufferedImage readImageFromDisk(String ImagePath) throws IOException {
		File input = new File(getImagePath(ImagePath));
		BufferedImage image = ImageIO.read(input);
		return image;
	} */

/* 	public static boolean sendImage(BufferedImage image, ObjectOutputStream out) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, getImageFileExtension(image), baos);
			out.writeObject(baos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Sent image to server.");
		return true;
	} */

	public static String getImageFileExtension(BufferedImage image) {
		if (image == null) {
			return "";
		}
		String extension = "";
		switch (image.getType()) {
			case BufferedImage.TYPE_3BYTE_BGR:
			case BufferedImage.TYPE_4BYTE_ABGR:
				extension = "png";
				break;
			default:
				extension = "jpg";
				break;
		}
		return extension;
	}

	public static BufferedImage readImageFromDisk(String ImagePath) throws IOException {
		File input = new File(getImagePath(ImagePath));
		BufferedImage image = ImageIO.read(input);
		return image;
	}

	public static String getImagePath(String ImageName) {
		Path basePath = Paths.get(new File("").getAbsolutePath());
		Path imagePath = Paths.get(ImageName);
		Path fullPath = basePath.resolve(imagePath);
		return fullPath.toString();
	}

	public static File createFolder(String name) {
		// Create a folder for the images
		File folder = new File(name);
		if (!folder.exists()) {
			folder.mkdir();
			System.out.println("Created folder: server-images");
		}
		return folder;
	}

	public static String writeImageToFile(File folder, BufferedImage image, String extension) throws IOException {
		// String fileName = "server-image-" + System.currentTimeMillis() + "." +
		// extension;
		String fileName = "portao.jpg";
		File output = new File(folder, fileName);
		ImageIO.write(image, extension, output);
		return fileName;
	}

}
