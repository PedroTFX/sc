import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Hashtable;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Data {

	public static boolean updateImageWineFile(String lineToUpdate, String updatedLine) throws /* IO */Exception {
		return writeOnFile(updatedLine, Constants.WINE_IMAGE_FILE);
	}

	public static String readUserInfoFromFile(String userName/* , Key secretKey */) throws /* IO */Exception/* , InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException */ {
		String line = null;
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(Constants.USER_FILE));
		while ((line = br.readLine()) != null) {
			String decryptedLine = decryptAES128(line);
			if ((decryptedLine.equals(""))) {
				br.close();
				return null;
			}
			String[] userInfo = decryptedLine.split(":");
			if (userInfo[0].equals(userName)) {
				br.close();
				return decryptedLine;
			}
		}
		br.close();
		return null;
	}

	public static boolean confirmPassword(String user, String password/* , key secretKey */) throws /* IO */Exception {
		String info = readUserInfoFromFile(user);
		if (info != null) {
			String dataBasePassword = info.split(":")[1];
			return password.equals(dataBasePassword);
		} else {
			return writeOnFile(encryptAES128(user + ":" + password + ":" +/*  Constants.STARTING_BALANCE */Integer.toString(200 + new Random().nextInt(201))), Constants.USER_FILE);
		}
	}

	public static boolean registerUser(String userInfo) throws /* IO */Exception {
		return writeOnFile(userInfo, Constants.USER_FILE);
	}

	public static String readWineInfoFromFile(String key) throws IOException {
		String line = null;
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(Constants.WINE_FILE));
		while ((line = br.readLine()) != null) {
			String[] userInfo = line.split(":");
			if (userInfo[0].equals(key)) {
				br.close();
				return line;
			}
		}
		br.close();
		return null;
	}

	public static boolean updateLineWines(String toUpdate, String updated) throws IOException {

		BufferedReader file = new BufferedReader(new FileReader(new File(Constants.WINE_FILE)));

		String line;
		String input = "";
		// read file line by line and replace
		while ((line = file.readLine()) != null) {
			input += line + "\n";
		}

		input = input.replace(toUpdate, updated);

		FileOutputStream os = new FileOutputStream(new File(Constants.WINE_FILE));
		os.write(input.getBytes());

		file.close();
		os.close();
		return true;
	}

	public static boolean updateLineUsers(String toUpdate, String updated) throws IOException {
		BufferedReader file = new BufferedReader(new FileReader(new File(Constants.USER_FILE)));

		String line;
		String input = "";
		// read file line by line and replace
		while ((line = file.readLine()) != null) {
			input += line + "\n";
		}

		input = input.replace(toUpdate, updated);

		FileOutputStream os = new FileOutputStream(new File(Constants.USER_FILE));
		os.write(input.getBytes());

		file.close();
		os.close();
		return true;
	}

	public static boolean writeOnFile(String line, String fileName) throws /* IO */Exception {
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true));
		bw.write(line);
		bw.flush();
		bw.newLine();
		bw.close();
		return true;
	}

	private static String encryptAES128(String line) throws RuntimeException {
		try {
			SecretKeySpec secretKey = new SecretKeySpec("yomamakeydeusman".getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			byte[] encryptedBytes = cipher.doFinal(line.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(encryptedBytes);
		} catch (Exception e) {
			throw new RuntimeException("Error while encrypting line", e);
		}
	}

	private static String decryptAES128(String encryptedLine) throws RuntimeException {
		try {
			SecretKeySpec secretKey = new SecretKeySpec("yomamakeydeusman".getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] decodedBytes = Base64.getDecoder().decode(encryptedLine);
			byte[] decryptedBytes = cipher.doFinal(decodedBytes);
			return new String(decryptedBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new RuntimeException("Error while decrypting line", e);
		}
	}

	public static void main(String[] args) {
		String originalLine = "This is a line to be encrypted";
		String encryptedLine = encryptAES128(originalLine);
		System.out.println("Encrypted line: " + encryptedLine);

		String decryptedLine = decryptAES128(encryptedLine);
		System.out.println("Decrypted line: " + decryptedLine);
	}

	public static String readImageNameFromWineImageFile(String exists) throws IOException {
		String line = null;
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(Constants.WINE_IMAGE_FILE));
		while ((line = br.readLine()) != null) {
			if ((line.equals(""))) {
				br.close();
				return null;
			}
			String[] fileInfo = line.split(":");
			if (fileInfo[0].equals(exists)) {
				br.close();
				return fileInfo[1];
			}
		}
		br.close();
		return null;
	}

	public static String readSellInfo(String wine, String seller) throws IOException {
		String line = null;
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(Constants.SELLS_FILE));
		while ((line = br.readLine()) != null) {
			if ((line.equals(""))) {
				br.close();
				return null;
			}
			String[] fileInfo = line.split(":");
			if (fileInfo[0].equals(wine) && fileInfo[1].equals(seller)) {
				br.close();
				return line;
			}
		}
		br.close();
		return null;
	}

	public static boolean updateImageSellsFile(String toUpdate, String updated) throws IOException {
		BufferedReader file = new BufferedReader(new FileReader(new File(Constants.SELLS_FILE)));

		String line;
		String input = "";
		// read file line by line and replace
		while ((line = file.readLine()) != null) {
			input += line + "\n";
		}
		input = input.replace(toUpdate, updated);

		FileOutputStream os = new FileOutputStream(new File(Constants.SELLS_FILE));
		os.write(input.getBytes());

		file.close();
		os.close();
		return true;
	}

	public static boolean updateWineStock(String wine, String user, int newStock, String seller) throws /* IO */Exception {
		String userInfo = Data.readSellInfo(wine, seller);
		String[] userInfoTokens = userInfo.split(":");
		userInfoTokens[2] = String.valueOf(newStock);
		return Data.updateImageSellsFile(userInfo, String.join(":", userInfoTokens));
	}

	public static boolean updateUserBalance(String userId, int newSellerBalance) throws /* IO */Exception {
		String userInfo = Data.readUserInfoFromFile(userId);
		String[] userInfoTokens = userInfo.split(":");
		userInfoTokens[2] = String.valueOf(newSellerBalance);
		return Data.updateLineUsers(userInfo, String.join(":", userInfoTokens));
	}

	/**
	 * Creates a folder with the given name if it doesn't exist.
	 *
	 * @param name
	 * @return
	 */
	public static Boolean createFolder(String name) {
		// Create a folder for the images
		File folder = new File(name);
		if (!folder.exists()) {
			folder.mkdir();
			System.out.println("Created folder: " + name);
		}
		return folder.exists();
	}

	public static void createDBs() {
		try {
			Boolean folderCreated = createFolder(Constants.DB_FOLDER);
			Boolean usersFileCreated = new File(Constants.USER_FILE).createNewFile();
			Boolean winesFileCreated = new File(Constants.WINE_FILE).createNewFile();
			Boolean sellsFileCreated = new File(Constants.SELLS_FILE).createNewFile();
			Boolean wineImageFileCreated = new File(Constants.WINE_IMAGE_FILE)
					.createNewFile();
			Boolean messagesFileCreated = new File(Constants.MESSAGE_FILE).createNewFile();

			String folderCreatedString = folderCreated ? "Folder created ✅" : "Folder not created ❌";
			String usersFileCreatedString = usersFileCreated ? "Users file created ✅" : "Users file not created ❌";
			String winesFileCreatedString = winesFileCreated ? "Wines file created ✅" : "Wines file not created ❌";
			String sellsFileCreatedString = sellsFileCreated ? "Sales file created ✅" : "Sales file not created ❌";
			String wineImageFileCreatedString = wineImageFileCreated ? "Wine image file created ✅"
					: "Wine image file not created ❌";
			String messagesFileCreatedString = messagesFileCreated ? "Messages file created ✅"
					: "Messages file not created ❌";
			if (folderCreated && usersFileCreated && winesFileCreated && sellsFileCreated && wineImageFileCreated
					&& messagesFileCreated) {

				System.out.println(String.format("%s", folderCreatedString));
				System.out.println(String.format("%s", usersFileCreatedString));
				System.out.println(String.format("%s", winesFileCreatedString));
				System.out.println(String.format("%s", sellsFileCreatedString));
				System.out.println(String.format("%s", wineImageFileCreatedString));
				System.out.println(String.format("%s", messagesFileCreatedString));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean updateMessagesFile(String toUpdate, String updated) throws IOException {
		BufferedReader file = new BufferedReader(new FileReader(new File(Constants.MESSAGE_FILE)));

		String line;
		String input = "";
		// read file line by line and replace
		while ((line = file.readLine()) != null) {
			input += line + "\n";
		}
		input = input.replace(toUpdate + "\n", updated);

		FileOutputStream os = new FileOutputStream(new File(Constants.MESSAGE_FILE));
		os.write(input.getBytes());

		file.close();
		os.close();

		return true;
	}

	public static Hashtable<String, ArrayList<String>> readMessagesFromFile(String receiver) throws IOException {
		Hashtable<String, ArrayList<String>> messages = new Hashtable<String, ArrayList<String>>();
		String line = null;
		BufferedReader br = new BufferedReader(new FileReader(Constants.MESSAGE_FILE));
		while ((line = br.readLine()) != null) {
			String[] fileInfo = line.split(":");
			if(!fileInfo[0].equals(receiver)){
				continue;
			}

			String sender = fileInfo[1];
			String[] remainingTokens = Arrays.copyOfRange(fileInfo, 2, fileInfo.length);
			String message = String.join(":", remainingTokens);
			if(!messages.containsKey(sender)) {
				messages.put(sender, new ArrayList<String>());
			}

			messages.get(sender).add(Data.unescape(message));
		}
		br.close();

		// Remover mensagens do ficheiro
		for (String sender : messages.keySet()) {
			for(String message : messages.get(sender)) {
				String toUpdate = String.format("%s:%s:%s", receiver, sender, escape(message));
				Data.updateMessagesFile(toUpdate, "");
			}
		}

		return messages;
	}

	static String escape(String message) {
		return message.replace(":", "\\:");
	}

	static String unescape(String message) {
		return message.replace("\\:", ":");
	}
}
