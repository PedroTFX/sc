import java.io.*;
import java.net.*;

public class NewClient {
	private static String SERVER_IP;
	private static final int SERVER_PORT = 12345;

	public static void main(String[] args) throws IOException {
		SERVER_IP = args[0];
		Socket socket = null;
		BufferedReader inputReader = null;
		PrintWriter out = null;

		try {
			// Connect to the server
			socket = new Socket(SERVER_IP, SERVER_PORT);

			// Set up input and output streams for socket
			out = new PrintWriter(socket.getOutputStream(), true);
			inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// Set up console input reader
			BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

			String userInput;
			while ((userInput = consoleReader.readLine()) != null) {
				out.println(userInput);
				System.out.println("Server response: " + inputReader.readLine());

				if (userInput.equals("exit")) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// Close everything
			if (inputReader != null) {
				inputReader.close();
			}
			if (out != null) {
				out.close();
			}
			if (socket != null) {
				socket.close();
			}
		}
	}
}
