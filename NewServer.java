import java.io.*;
import java.net.*;

public class NewServer {
	private static final int PORT = 8080;

	public static void main(String[] args) {
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			System.out.println("Server is running on port " + PORT);

			while (true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("New client connected");

				new ClientHandler(clientSocket).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class ClientHandler extends Thread {
	private Socket clientSocket;

	public ClientHandler(Socket socket) {
		this.clientSocket = socket;
	}

	@Override
	public void run() {
		try (
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);) {
			String request, response;
			while ((request = in.readLine()) != null) {
				if ("exit".equalsIgnoreCase(request)) {
					break;
				}

				// Process the request from the client
				response = processRequest(request);

				// Send a response back to the client
				out.println(response);
			}
		} catch (IOException e) {
			System.err.println("IO exception in client handler");
			System.err.println(e.getStackTrace());
		} finally {
			try {
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String processRequest(String request) {
		// Implement your request handling logic here.
		return "Server processed the request: " + request;
	}
}
