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

	public DB(String cipher) throws Exception {
		user = new Table(Constants.USER_FILE, cipher);
		wine = new Table(Constants.WINE_FILE);
		listing = new Table(Constants.WINE_LISTINGS_FILE);
		message = new Table(Constants.MESSAGE_FILE);

		// Initialize database
		initFiles();

		// Verify file integrity
		verifyFiles();
	}

	private void initFiles() throws Exception {
		try {
			// Create base folder
			File baseFolder = new File(Constants.DB_FOLDER);
			if (!baseFolder.exists()) {
				baseFolder.mkdir();
				System.out.println("Base folder created ✅");
			}

			File imagesFolder = new File(Constants.SERVER_IMAGES_FOLDER);
			if (!imagesFolder.exists()) {
				imagesFolder.mkdir();
				System.out.println("Images folder created ✅");
			}

			File blockchainFolder = new File(Constants.BLOCKCHAIN_FOLDER);
			if (!blockchainFolder.exists()) {
				blockchainFolder.mkdir();
				System.out.println("Blockchain folder created ✅");
			}

			// Create message files
			if (new File(Constants.USER_FILE + ".txt").createNewFile()) {
				System.out.println(Constants.USER_FILE + " file was created!");
				Integrity.create(Constants.USER_FILE);
				System.out.println(Constants.USER_FILE + " hash file was created!");
			}
			if (new File(Constants.WINE_FILE + ".txt").createNewFile()) {
				System.out.println(Constants.WINE_FILE + " file was created!");
				Integrity.create(Constants.WINE_FILE);
				System.out.println(Constants.WINE_FILE + " hash file was created!");
			}
			if (new File(Constants.WINE_LISTINGS_FILE + ".txt").createNewFile()) {
				System.out.println(Constants.WINE_LISTINGS_FILE + " file was created!");
				Integrity.create(Constants.WINE_LISTINGS_FILE);
				System.out.println(Constants.WINE_LISTINGS_FILE + " hash file was created!");
			}
			if (new File(Constants.MESSAGE_FILE + ".txt").createNewFile()) {
				System.out.println(Constants.MESSAGE_FILE + " file was created!");
				Integrity.create(Constants.MESSAGE_FILE);
				System.out.println(Constants.MESSAGE_FILE + " hash file was created!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void verifyFiles() {
		try {
			System.out.println("User file " + (Integrity.verify(Constants.USER_FILE) ? "verified!" : "not verified!"));
			System.out.println("Wine file " + (Integrity.verify(Constants.WINE_FILE) ? "verified!" : "not verified!"));
			System.out.println("Wine listings file "
					+ (Integrity.verify(Constants.WINE_LISTINGS_FILE) ? "verified!" : "not verified!"));
			System.out.println(
					"Message file " + (Integrity.verify(Constants.MESSAGE_FILE) ? "verified!" : "not verified!"));
		} catch (Exception e) {
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
			BufferedWriter bw = new BufferedWriter(new FileWriter(file + ".txt", true));
			bw.write(rowToAdd);
			bw.flush();
			bw.newLine();
			bw.close();
			Integrity.create(file);
		}

		public String get(String id) throws Exception {
			if (!Integrity.verify(file)) {
				throw new Exception("Ficheiro " + file + " foi corrompido!");
			}
			BufferedReader br = new BufferedReader(new FileReader(file + ".txt"));
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
			if (!Integrity.verify(file)) {
				throw new Exception("Ficheiro " + file + " foi corrompido!");
			}
			BufferedReader br = new BufferedReader(new FileReader(file + ".txt"));
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
			if (!Integrity.verify(file)) {
				throw new Exception("Ficheiro " + file + " foi corrompido!");
			}
			BufferedReader br = new BufferedReader(new FileReader(file + ".txt"));
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
			if (!Integrity.verify(file)) {
				throw new Exception("Ficheiro " + file + " foi corrompido!");
			}
			ArrayList<String> fileContent = new ArrayList<String>(Files.readAllLines(Path.of(file + ".txt")));
			String newRow = cipher == null ? row : SecurityRSA.encryptAES128(row, cipher);
			for (int i = 0; i < fileContent.size(); i++) {
				String currentLine = cipher == null ? fileContent.get(i)
						: SecurityRSA.decryptAES128(fileContent.get(i), cipher);
				if (currentLine.startsWith(id + ":")) {
					fileContent.set(i, newRow);
					break;
				}
			}
			Files.write(Path.of(file + ".txt"), fileContent);
			Integrity.create(file);
			return row;
		}
	}
}
