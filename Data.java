import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
		return writeOnFile(updatedLine, WINE_IMAGE_FILE);
	}

	public static String readUserInfoFromFile(String key) throws IOException{
		String line = null;
		BufferedReader br = null;
			br = new BufferedReader(new FileReader(USER_FILE));
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
			return writeOnFile(user + ":" + password + ":" + STARTING_BALANCE, USER_FILE);
		}
		//return readUserInfoFromFile(user).split(":")[1].equals(password);
    }

	public static boolean registerUser(String userInfo) throws IOException {
		return writeOnFile(userInfo, USER_FILE);
	}

	public static String readWineInfoFromFile(String key) throws IOException{
		String line = null;
		BufferedReader br = null;
			br = new BufferedReader(new FileReader(WINE_FILE));
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

		BufferedReader file = new BufferedReader(new FileReader(new File(WINE_FILE)));

		String line;
		String input = "";
		//read file line by line and replace
		while ((line = file.readLine()) != null){
			input += line + "\n";
		}
		input = input.replace(toUpdate, updated);

		FileOutputStream os = new FileOutputStream(new File(WINE_FILE));
		os.write(input.getBytes());

		file.close();
		os.close();
		return true;
	}

	public static boolean updateLineUsers(String toUpdate, String updated) throws IOException {
		BufferedReader file = new BufferedReader(new FileReader(new File(USER_FILE)));

		String line;
		String input = "";
		//read file line by line and replace
		while ((line = file.readLine()) != null){
			input += line + "\n";
		}
		input = input.replace(toUpdate, updated);

		FileOutputStream os = new FileOutputStream(new File(USER_FILE));
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
	/**
	 *
	 * TESTES
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
/* 		String line0 = readUserInfoFromFile("user2");

		String line = readUserInfoFromFile("user1");

		String line2 = readUserInfoFromFile("user2");

		String line3 = readUserInfoFromFile("user");

		String line4 = readUserInfoFromFile("user10");

		String line5 = readUserInfoFromFile("user2");

		System.out.println(line0);
		System.out.println(line);
		System.out.println(line2);
		System.out.println(line3);
		System.out.println(line4);
		System.out.println(line5);

		System.out.println("VINHOS");

		String wine0 = readWineInfoFromFile("wine");

		String wine = readWineInfoFromFile("wine1");

		String wine2 = readWineInfoFromFile("sporting");

		String wine3 = readWineInfoFromFile("portinho");

		String wine4 = readWineInfoFromFile("benfica");

		String wine5 = readWineInfoFromFile("wine2");

		System.out.println(wine0);
		System.out.println(wine);
		System.out.println(wine2);
		System.out.println(wine3);
		System.out.println(wine4);
		System.out.println(wine5);

		*/
		System.out.println("ESCRITAS");

		updateLineWines("porto:10:100:1,2,3,4,5", "porto:10999:1000000:1,2,3,4,5");
		updateLineUsers("user30:pass:100:vinho1,vinho2:es lindo", "user30:pass:100:vinho1,vinho2:es feio");


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
		br = new BufferedReader(new FileReader(WINE_IMAGE_FILE));
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
}



/*
wine1:100;200;2,3,4
portinho:69,69,3,5,1,2
benfica:7,7,1,2,3,4,5
*/
