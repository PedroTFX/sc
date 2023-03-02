import java.util.Scanner;

public class Tintolmarket {
	private static Scanner sc = new Scanner(System.in);

	public static void main(String[] args) {
		// Verificar se temos pelo menos 2 argumentos
		if(args.length < 2) {
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

	private Tintolmarket(String serverAddress, int serverPort, String userId, String password) {

	}
}
