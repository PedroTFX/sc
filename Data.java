import java.io.BufferedReader;
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
	public static String readInfoFromFile(String key, String file){
		String line = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				String[] userInfo = line.split(":");
				if (userInfo[0].equals(key)) {
					br.close();
					return userInfo[1];
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static void updateLine(String toUpdate, String updated, String file_name) throws IOException {
		BufferedReader file = new BufferedReader(new FileReader(new File(file_name)));
		String line;
		String input = "";

		while ((line = file.readLine()) != null){
			input += line + "\n";
		}

		input = input.replace(toUpdate, updated);

		FileOutputStream os = new FileOutputStream(new File(file_name));
		os.write(input.getBytes());

		file.close();
		os.close();
	}

	/**
	 *
	 * TESTES
	 * @param args
	 */
	public static void main(String[] args) {
		String line0 = readInfoFromFile("user2", "users.txt");

		String line = readInfoFromFile("user1", "users.txt");

		String line2 = readInfoFromFile("user2", "users.txt");

		String line3 = readInfoFromFile("user", "users.txt");

		String line4 = readInfoFromFile("user10", "users.txt");

		String line5 = readInfoFromFile("user2", "users.txt");

		System.out.println(line0);
		System.out.println(line);
		System.out.println(line2);
		System.out.println(line3);
		System.out.println(line4);
		System.out.println(line5);

		System.out.println("VINHOS");

		String wine0 = readInfoFromFile("wine", "wines.txt");

		String wine = readInfoFromFile("wine1", "wines.txt");

		String wine2 = readInfoFromFile("sporting", "wines.txt");

		String wine3 = readInfoFromFile("portinho", "wines.txt");

		String wine4 = readInfoFromFile("benfica", "wines.txt");

		String wine5 = readInfoFromFile("wine2", "wines.txt");

		System.out.println(wine0);
		System.out.println(wine);
		System.out.println(wine2);
		System.out.println(wine3);
		System.out.println(wine4);
		System.out.println(wine5);

		System.out.println("ESCRITAS");


		try {
			updateLine("wine1:" + readInfoFromFile("wine1", "wines.txt"), "gil_vicente", "wines.txt");

			Charset charset = StandardCharsets.UTF_8;
			String content = new String(Files.readAllBytes(Paths.get("wines.txt")), charset);
			content = content.replaceAll("portinho:69,69,3,5,1,2", "replace all");
			Files.write(Paths.get("wines.txt"), content.getBytes(charset));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}



/*
wine1:100;200;2,3,4
portinho:69,69,3,5,1,2
benfica:7,7,1,2,3,4,5
*/
