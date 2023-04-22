import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;


public class DB {
	public Table user = null;
	public Table wine = null;
	public Table listing = null;
	public Table message = null;

	public DB(String cipher) {
		user = new Table(Constants.USER_FILE, cipher);
		wine = new Table(Constants.WINE_FILE);
		listing = new Table(Constants.WINE_LISTINGS_FILE);
		message = new Table(Constants.MESSAGE_FILE);

		// Initialize database
		createDBs();
	}

	public void createDBs() {
		try {
			// Create base folder
			File baseFolder = new File(Constants.DB_FOLDER);
			if (!baseFolder.exists()) {
				baseFolder.mkdir();
				System.out.println("Base folder created ✅");
			} else {
				System.out.println("Base folder was already created ✅");
			}

			File imagesFolder = new File(Constants.SERVER_IMAGES_FOLDER);
			if (!imagesFolder.exists()) {
				imagesFolder.mkdir();
				System.out.println("Images folder created ✅");
			} else {
				System.out.println("Images folder was already created ✅");
			}

			// Create files
			Boolean usersFileCreated = new File(Constants.USER_FILE).createNewFile();
			Boolean winesFileCreated = new File(Constants.WINE_FILE).createNewFile();
			Boolean sellsFileCreated = new File(Constants.WINE_LISTINGS_FILE).createNewFile();
			Boolean messagesFileCreated = new File(Constants.MESSAGE_FILE).createNewFile();

			// Print results
			String usersFileCreatedString = usersFileCreated ? "Users file created ✅" : "Users file not created ❌";
			String winesFileCreatedString = winesFileCreated ? "Wines file created ✅" : "Wines file not created ❌";
			String sellsFileCreatedString = sellsFileCreated ? "Sales file created ✅" : "Sales file not created ❌";
			String messagesFileCreatedString = messagesFileCreated ? "Messages file created ✅"
					: "Messages file not created ❌";
			if (usersFileCreated && winesFileCreated && sellsFileCreated
					&& messagesFileCreated) {
				System.out.println(String.format("%s", usersFileCreatedString));
				System.out.println(String.format("%s", winesFileCreatedString));
				System.out.println(String.format("%s", sellsFileCreatedString));
				System.out.println(String.format("%s", messagesFileCreatedString));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class Table {
		String file = null;
		String cipher = null;

		public Table(String file) {
			this.file = file;
		}

		public Table(String file, String cipher) {
			this(file);
			this.cipher = cipher;
		}

		public void add(String row) throws Exception {
			String rowToAdd = cipher == null ? row : SecurityRSA.encryptAES128(row, cipher);
			BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
			bw.write(rowToAdd);
			bw.flush();
			bw.newLine();
			bw.close();
		}

		public String get(String id) throws Exception {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null) {
				String currentLine = cipher == null ? line : SecurityRSA.decryptAES128(line, cipher);
				if ((currentLine.equals(""))) {
					br.close();
					return null;
				}
				String[] userInfo = currentLine.split(":");
				if (userInfo[0].equals(id)) {
					br.close();
					return currentLine;
				}
			}
			br.close();
			return null;
		}

		public String get(String wineName, String userName) throws Exception {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null) {
				String currentLine = cipher == null ? line : SecurityRSA.decryptAES128(line, cipher);
				if ((currentLine.equals(""))) {
					br.close();
					return null;
				}
				String[] userInfo = currentLine.split(":");
				if (userInfo[0].equals(wineName) && userInfo[1].equals(userName)) {
					br.close();
					return currentLine;
				}
			}
			br.close();
			return null;
		}

		public ArrayList<String> getAll(String id) throws Exception {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			ArrayList<String> lines = new ArrayList<String>();
			while ((line = br.readLine()) != null) {
				String currentLine = cipher == null ? line : SecurityRSA.decryptAES128(line, cipher);
				if ((currentLine.equals(""))) {
					br.close();
					return null;
				}
				String[] userInfo = currentLine.split(":");
				if (userInfo[0].equals(id)) {
					lines.add(currentLine);
				}
			}
			br.close();
			return lines;
		}

		public String update(String id, String row) throws Exception {
			ArrayList<String> fileContent = new ArrayList<String>(Files.readAllLines(Path.of(file)));
			String newRow = cipher == null ? row : SecurityRSA.encryptAES128(row, cipher);
			for (int i = 0; i < fileContent.size(); i++) {
				String currentLine = cipher == null ? fileContent.get(i) : SecurityRSA.decryptAES128(fileContent.get(i), cipher);
				if (currentLine.startsWith(id + ":")) {
					fileContent.set(i, newRow);
					break;
				}
			}
			Files.write(Path.of(file), fileContent);
			return row;
		}
	}
}
