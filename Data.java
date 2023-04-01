import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

public class Data {

	public static final String IMAGES_FOLDER = "server-images\\";
	public static final String DB_FOLDER = "db";
	public static final String USER_FILE = DB_FOLDER + "\\" + "users.txt";
	public static final String WINE_FILE = DB_FOLDER + "\\" + "wines.txt";
	public static final String SELLS_FILE = DB_FOLDER + "\\" + "sells.txt";
	public static final String WINE_IMAGE_FILE = DB_FOLDER + "\\" + "wine_image.txt";
	public static final String MESSAGE_FILE = DB_FOLDER + "\\" + "messages.txt";
	public static final String STARTING_BALANCE = "200";

	public static boolean updateImageWineFile(String lineToUpdate, String updatedLine) throws IOException {
		return writeOnFile(updatedLine, WINE_IMAGE_FILE);
	}

	/**
	 * Reads from the file (String filename) for the keywords (String keyWords) that are mapped 
	 * like this: keyword1:keyword2:keyword3
	 * the function divides the keyWords by ":" and searches for the line that contains all the keywords in order
	 * @param key
	 * @return
	 * @throws IOException
	 */
	private static String findInFile(String keyWords, String filename) throws IOException{
		// init
		String line = null;
		BufferedReader buffer = new BufferedReader(new FileReader(filename));
		boolean found = false;

		// read buffer line by line till find a line that contains all the keywords
		while ((line = buffer.readLine()) != null && !found) {
			if ((line.equals(""))) {
				buffer.close();
				return null;
			}

			// split the line by ":" and check size
			String[] userInfo = line.split(":");
			String[] keyWordsArray = keyWords.split(":");
			if(keyWordsArray.length > userInfo.length){
				buffer.close();
				return null;
			}
			
			// check if the line contains all the keywords in order
			for(int i = 0; i < keyWordsArray.length; i++){
				if(!userInfo[i].equals(keyWordsArray[i])){
					break;
				}
				if(i == keyWordsArray.length - 1){
					found = true;
					buffer.close();
					return line;
				}
			}
		}
		
		buffer.close();
		return null;
	}

	public static String readUserInfoFromFile(String key) throws IOException {
		return findInFile(key, USER_FILE);
	}

	/**
	 * Reads from the user file and checks if the password is correct
	 * Returns false if the user does not exist or the password is incorrect or null
	 * @param user
	 * @param password
	 * @return
	 * @throws IOException
	 */
	public static boolean confirmPassword(String user, String password) throws IOException {
		String userInfo = readUserInfoFromFile(user);
		if(userInfo == null){
			return false;
		}
		return (password == null) ? false : userInfo.split(":")[1].equals(password);
	}

	/**
	 * Writes on the user file the new user
	 * @param userInfo
	 * @return
	 * @throws IOException
	 */
	public static boolean registerUser(String userInfo) throws IOException {
		return writeOnFile(userInfo, USER_FILE);
	}

	/**
	 * Reads from the wine file and checks if the wine exists
	 * Returns false if the wine does not exist or null
	 * @param wine
	 * @return
	 * @throws IOException
	 */
	public static String readWineInfoFromFile(String wine) throws IOException {
		return findInFile(wine, WINE_FILE);
	}

	/**
	 * Reads the whole file and replaces the line (String searchFor), with the line (String updateTo)
	 * returns true if the line was updated, false otherwise
	 * @param toUpdate
	 * @param updated
	 * @param filename
	 * @throws IOException
	 */
	private static boolean updateLineInFile(String searchFor, String updateTo, String filename) throws IOException{
		// init
		BufferedReader buffer = new BufferedReader(new FileReader(filename));
		FileOutputStream out = new FileOutputStream(filename);
		String fileInfo = "";
		String line;

		// read buffer line by line to populate fileInfo
		while ((line = buffer.readLine()) != null) {
			fileInfo += line + "\n";
		}

		// replace the line in fileInfo and write it to the file
		fileInfo = fileInfo.replace(searchFor, updateTo);
		out.write(fileInfo.getBytes());

		buffer.close();
		out.close();
		return findInFile(searchFor, filename).equals(searchFor);
	}

	/**
	 * Writes on the wine file the updated line
	 * @param wineInfo
	 * @return
	 * @throws IOException
	 */
	public static boolean updateLineWines(String toUpdate, String updated) throws IOException {
		return updateLineInFile(toUpdate, updated, WINE_FILE);
	}

	/**
	 * Writes on the user file the new updated user line 
	 * @param wineInfo
	 * @return
	 * @throws IOException
	 */
	public static boolean updateLineUsers(String toUpdate, String updated) throws IOException {
		return updateLineInFile(toUpdate, updated, USER_FILE);
	}

	/**
	 * Writes on the file (String) filename the (String) line 
	 * @param wineInfo
	 * @return
	 * @throws IOException
	 */
	public static boolean writeOnFile(String line, String fileName) throws IOException {
		BufferedWriter buffer = new BufferedWriter(new FileWriter(fileName, true));
		buffer.write(line);
		buffer.flush();
		// Revoke newLine() method
		buffer.newLine();
		buffer.close();
		return true;
	}



	public static String readImageNameFromWineImageFile(String exists) throws IOException {
		return findInFile(exists, WINE_IMAGE_FILE).split(":")[1];
	}

	public static String readSellInfo(String user, String wine) throws IOException {
		return findInFile(user + ":" + wine, SELLS_FILE);
	}

	// TODO UPDATE FUNCTION NAME
	public static boolean updateSellsFile(String toUpdate, String updated) throws IOException {
		return updateLineInFile(toUpdate, updated, SELLS_FILE);
	}

	public static boolean updateWineStock(String wine, String user, int newStock) throws IOException {
		String userInfo = Data.readSellInfo(user, wine);
		String[] userInfoTokens = userInfo.split(":");
		userInfoTokens[2] = String.valueOf(newStock);
		return Data.updateSellsFile(userInfo, String.join(":", userInfoTokens));
	}

	public static boolean updateUserBalance(String userId, int newSellerBalance) throws IOException {
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
			Boolean folderCreated = createFolder(DB_FOLDER);
			Boolean usersFileCreated = new File(USER_FILE).createNewFile();
			Boolean winesFileCreated = new File(WINE_FILE).createNewFile();
			Boolean sellsFileCreated = new File(SELLS_FILE).createNewFile();
			Boolean wineImageFileCreated = new File(WINE_IMAGE_FILE)
					.createNewFile();
			Boolean messagesFileCreated = new File(MESSAGE_FILE).createNewFile();

			String folderCreatedString = folderCreated ? "Folder created" : "Folder not created";
			String usersFileCreatedString = usersFileCreated ? "Users file created" : "Users file not created";
			String winesFileCreatedString = winesFileCreated ? "Wines file created" : "Wines file not created";
			String sellsFileCreatedString = sellsFileCreated ? "Sales file created" : "Sales file not created";
			String wineImageFileCreatedString = wineImageFileCreated ? "Wine image file created"
					: "Wine image file not created";
			String messagesFileCreatedString = messagesFileCreated ? "Messages file created"
					: "Messages file not created";
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
		return updateLineInFile(toUpdate, updated, MESSAGE_FILE);
	}

	public static Hashtable<String, ArrayList<String>> readMessagesFromFile(String receiver) throws IOException {
		Hashtable<String, ArrayList<String>> messages = new Hashtable<String, ArrayList<String>>();
		String line = null;
		BufferedReader br = new BufferedReader(new FileReader(MESSAGE_FILE));
		while ((line = br.readLine()) != null) {
			String[] fileInfo = line.split(":");
			if(!fileInfo[0].equals(receiver)){
				continue;
			}

			// update msm
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

/*
 * wine1:100;200;2,3,4
 * portinho:69,69,3,5,1,2
 * benfica:7,7,1,2,3,4,5
 */
