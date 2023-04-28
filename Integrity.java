import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;

public class Integrity {

	public static void main(String[] args) throws Exception {
		String message = "Ah e tal e coiso";
		String hash = base64Hash(message);
		System.out.println(message);
		System.out.println(hash);
		System.out.println(compare(message, hash));
	}

	public static void create(String filename) throws Exception {
		String message = getFileContents(filename + ".txt");
		String base64Hash = base64Hash(message);
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename + ".hash"));
		bw.write(base64Hash);
		bw.close();
	}

	public static boolean verify(String filename) throws Exception {
		String message = getFileContents(filename + ".txt");
		String base64Hash = getFileContents(filename + ".hash");
		return compare(message, base64Hash);
	}

	private static byte[] hash(byte[] bytes) throws Exception {
		MessageDigest messageDigest = MessageDigest.getInstance("SHA");
		byte[] digest = messageDigest.digest(bytes);
		return digest;
	}

	public static String base64Hash(String message) throws Exception {
		return Base64.getEncoder().encodeToString(hash(message.getBytes()));
	}

	private static String getFileContents(String filename) throws IOException {
		byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
		return new String(fileBytes);
	}

	private static boolean compare(String message, String base64Hash) throws Exception {
		return base64Hash(message).equals(base64Hash);
	}

	public static String calculateFileHash(String filePath, String algorithm) throws Exception {
		MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
		byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
		byte[] digest = messageDigest.digest(fileBytes);

		StringBuilder sb = new StringBuilder();
		for (byte b : digest) {
			sb.append(String.format("%02x", b));
		}

		return sb.toString();
	}

	/*


	public static boolean verifyIntegrity(String filePath, String algorithm, String hashFilePath)
			throws IOException, NoSuchAlgorithmException {
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

	public static void updateHashValue(String filePath, String algorithm, String hashFilePath) throws IOException, NoSuchAlgorithmException {
		String fileHash = calculateFileHash(filePath, algorithm);

		// Write the hash value to a separate file
		Files.write(Paths.get(hashFilePath), fileHash.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	public static String getAbsolutePath(String filePath) {
		Path path = Paths.get(filePath).toAbsolutePath().normalize();
		return path.toString();
	}

	public boolean verifyIntegrity(String filePath, String algorithm, String hashFilePath) throws IOException, NoSuchAlgorithmException {
		// Read the stored hash value from the separate file
		String storedHashValue = new String(Files.readAllBytes(Paths.get(hashFilePath)));

		// Calculate the current hash value of the file
		String currentHashValue = calculateFileHash(filePath, algorithm);

		return storedHashValue.equals(currentHashValue);
	} */
}
