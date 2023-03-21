import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

public class Data {

 	public static final String USER_FILE = "users.txt";
	public static final String WINE_FILE = "wines.txt";
	public static final String WINE_IMAGE_FILE = "wine_image.txt";
	public static final String STARTING_BALANCE = "200";

	public static boolean updateImageWineFile(String lineToUpdate, String updatedLine) throws IOException{
/* 		BufferedReader file = new BufferedReader(new FileReader(new File(WINE_IMAGE_FILE)));

		String line;
		String input = "";
		// read file line by line and replace
		while ((line = file.readLine()) != null) {
			input += line + "\n";
		}
		input = input.replace(lineToUpdate, updatedLine);

		FileOutputStream os = new FileOutputStream(new File(WINE_IMAGE_FILE));
		os.write(input.getBytes());

		file.close();
		os.close();
		return true; */
		return writeOnFile(updatedLine, Constants.WINE_IMAGE_FILE);
	}

	public static String readUserInfoFromFile(String key) throws IOException{
		String line = null;
		BufferedReader br = null;
			br = new BufferedReader(new FileReader(Constants.USER_FILE));
			while ((line = br.readLine()) != null) {
				if ((line.equals(""))){
					br.close();
					return null;
				}
				String[] userInfo = line.split(":");
				if (userInfo[0].equals(key)) {
					br.close();
					return line;
				}
			}
			br.close();
		return null;
	}

    public static boolean confirmPassword(String user, String password) throws IOException {
		String info = readUserInfoFromFile(user);
		String DBPass = null;
		if (info != null) {
			DBPass = info.split(":")[1];
			return DBPass.equals(password);
		} else {
			return writeOnFile(user + ":" + password + ":" + Constants.STARTING_BALANCE, Constants.USER_FILE);
		}
		//return readUserInfoFromFile(user).split(":")[1].equals(password);
    }

	public static boolean registerUser(String userInfo) throws IOException {
		return writeOnFile(userInfo, Constants.USER_FILE);
	}

	public static String readWineInfoFromFile(String key) throws IOException{
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
		//read file line by line and replace
		while ((line = file.readLine()) != null){
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
		//read file line by line and replace
		while ((line = file.readLine()) != null){
			input += line + "\n";
		}
		input = input.replace(toUpdate, updated);

		FileOutputStream os = new FileOutputStream(new File(Constants.USER_FILE));
		os.write(input.getBytes());

		file.close();
		os.close();
		return true;
	}

	public static boolean writeOnFile(String line, String fileName) throws IOException{
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName,true));
		bw.write(line);
		bw.flush();
		// Revoke newLine() method
		bw.newLine();
		bw.close();
		return true;
    }

	public static void addNewWine(){
		//porto:portinho.jpg
	}

	public static Hashtable<String, String> getListings() {
		return null;
	}

	public static boolean updateListings(Hashtable<String, String> wineListings) {
		return false;
	}

	public static boolean sendMSM(String string) {
		return false;
	}

	public static String[] readMSM(String id) {
		return null;
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

	public static String readSellInfo(String wine) throws IOException {
		String line = null;
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(Constants.SELLS_FILE));
		while ((line = br.readLine()) != null) {
			if ((line.equals(""))) {
				br.close();
				return null;
			}
			String[] fileInfo = line.split(":");
			if (fileInfo[0].equals(wine)) {
				br.close();
				return fileInfo[1];
			}
		}
		br.close();
		return null;
	}

	public static boolean updateImageSellsFile(String toUpdate, String updated) throws IOException {
		BufferedReader file = new BufferedReader(new FileReader(new File(Constants.USER_FILE)));

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
}



/*
wine1:100;200;2,3,4
portinho:69,69,3,5,1,2
benfica:7,7,1,2,3,4,5
*/
