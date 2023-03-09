import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Data {

	private static String userfile = "users.txt";
	private static String wineFile = "wines.txt";

	public static String readUserInfoFromFile(String key) throws IOException{
		String line = null;
		BufferedReader br = null;
			br = new BufferedReader(new FileReader(userfile));
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

	
    public static boolean confirmPassword(String user, String password) throws IOException {
		return readUserInfoFromFile(user).split(":")[1].equals(password);
    }

	public static boolean registerUser(String userInfo) {
		return writeOnFile(userInfo, userfile);
	}

	public static String readWineInfoFromFile(String key) throws IOException{
		String line = null;
		BufferedReader br = null;
			br = new BufferedReader(new FileReader(wineFile));
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

	public static boolean updateLine(String toUpdate, String updated, String file_name) {
		BufferedReader file = null;
		try {
			file = new BufferedReader(new FileReader(new File(file_name)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		String line;
		String input = "";
		try {
			//read file line by line and replace 
			while ((line = file.readLine()) != null){
				input += line + "\n";
			}
			input = input.replace(toUpdate, updated);

			FileOutputStream os = new FileOutputStream(new File(file_name));
			os.write(input.getBytes());

			file.close();
			os.close();
			return true;

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean writeOnFile(String line, String fileName){
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(fileName,true));
            bw.write(line);
            // Revoke newLine() method
            bw.newLine();
            bw.close();
			return true;
        } catch (IOException e) {
            e.printStackTrace();
			return false;
        }
    }
	/**
	 *
	 * TESTES
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String line0 = readUserInfoFromFile("user2");

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

		System.out.println("ESCRITAS");


		try {
			updateLine("wine1:" + readWineInfoFromFile("wine1"), "gil_vicente", wineFile);

			Charset charset = StandardCharsets.UTF_8;
			String content = new String(Files.readAllBytes(Paths.get("wines.txt")), charset);
			content = content.replaceAll("portinho:69,69,3,5,1,2", "replace all");
			Files.write(Paths.get("wines.txt"), content.getBytes(charset));
		} catch (IOException e) {
			e.printStackTrace();
		}



	}





	
}



/*
wine1:100;200;2,3,4
portinho:69,69,3,5,1,2
benfica:7,7,1,2,3,4,5
*/
