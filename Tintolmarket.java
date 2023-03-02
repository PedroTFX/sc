import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Tintolmarket {
	private static Scanner sc = new Scanner(System.in);
	private Socket clientSocket = null;
	private ObjectInputStream in = null;
	private ObjectOutputStream out = null;

	public static void main(String[] args) {
		// Verificar se temos pelo menos 2 argumentos
		if (args.length < 2) {
			System.out.println("Utilização: java TintolMarket <server-address>[:port] <userId> [password]");
			return;
		}

		// Obter endereço e porta do servidor
		String[] serverAddressPort = args[0].split(":");
		String serverAddress = serverAddressPort[0];
		int serverPort = serverAddressPort.length > 1 ? Integer.parseInt(serverAddressPort[1]) : 12345;

		// Obter userID
		String userId = args[1];

		// Obter password
		String password = args.length > 2 ? args[2] : getPassword();

		// Lançar
		new Tintolmarket(serverAddress, serverPort, userId, password);
	}

	private static String getPassword() {
		System.out.print("Introduza a sua password: ");
		return sc.nextLine();
	}

	private Tintolmarket(String host, int port, String userId, String password) {
		initializeServerConnection(host, port);

		authenticateUser(userId, password);

		run();

		disconnectFromServer();

		close();
	}

	private void initializeServerConnection(String host, int port) {
		try {
			clientSocket = new Socket(host, port);
			in = new ObjectInputStream(clientSocket.getInputStream());
			out = new ObjectOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void authenticateUser(String userId, String password) {

	}

	private void run() {

	}

	private void disconnectFromServer() {
		try {
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void close() {

	}
}
