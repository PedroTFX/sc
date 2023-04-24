import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Integrity {

	public static void updateHashValue(String filePath, String algorithm, String hashFilePath) throws IOException, NoSuchAlgorithmException {
		String fileHash = calculateFileHash(filePath, algorithm);

		// Write the hash value to a separate file
		Files.write(Paths.get(hashFilePath), fileHash.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	public static String calculateFileHash(String filePath, String algorithm) throws IOException, NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
		byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
		byte[] digest = messageDigest.digest(fileBytes);

		StringBuilder sb = new StringBuilder();
		for (byte b : digest) {
			sb.append(String.format("%02x", b));
		}

		return sb.toString();
	}

	public static String getAbsolutePath(String filePath) {
		Path path = Paths.get(filePath).toAbsolutePath().normalize();
		return path.toString();
	}

	/* public boolean verifyIntegrity(String filePath, String algorithm, String hashFilePath) throws IOException, NoSuchAlgorithmException {
		// Read the stored hash value from the separate file
		String storedHashValue = new String(Files.readAllBytes(Paths.get(hashFilePath)));

		// Calculate the current hash value of the file
		String currentHashValue = calculateFileHash(filePath, algorithm);

		return storedHashValue.equals(currentHashValue);
	} */

	public static boolean verifyIntegrity(String filePath, String algorithm, String hashFilePath) throws IOException, NoSuchAlgorithmException {
		Path path = Paths.get(filePath);

		// Skip verification if the file is empty
		if (Files.size(path) == 0) {
			return true;
		}

		// Read the stored hash value from the separate file
		String storedHashValue = new String(Files.readAllBytes(Paths.get(hashFilePath)));

		// Calculate the current hash value of the file
		String currentHashValue = calculateFileHash(filePath, algorithm);

		return storedHashValue.equals(currentHashValue);
	}
}
