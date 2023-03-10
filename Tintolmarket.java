import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Scanner;

public class Tintolmarket implements Serializable {
	private static Scanner sc = new Scanner(System.in);
	private Socket clientSocket = null;
	private ObjectInputStream in = null;
	private ObjectOutputStream out = null;

	public static void main(String[] args) {
		// Verificar se temos pelo menos 2 argumentos
		if (args.length < 2) {
			System.out.println("Utilizacao: java TintolMarket <server-address>[:port] <userId> [password]");
			return;
		}

		// Obter endereço e porta do servidor
		String[] serverAddressPort = args[0].split(":");
		String serverAddress = serverAddressPort[0];
		int serverPort = serverAddressPort.length > 1 ? Integer.parseInt(serverAddressPort[1]) : 12345;

		// Obter userID and password
		String userId = args[1];
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
		Request authentication = Request.createAuthenticateOperation(userId, password);
		Response response = null;

		try {
			out.writeObject((Request) authentication);
			response = (Response) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		// Print error if there's one
		if (response.type == Response.Type.ERROR) {
			System.out.println(response.message);
			System.exit(1);
		}

		System.out.println("Utilizador autenticado " + response.message);
	}

	private void run() {
		while (true) {

			Request request;
			do {
				// Show menu
				menu();

				// Read command
				request = readRequest();
			} while (request == null);

			try {
				// Send request to server
				out.writeObject(request);

				// Receive response from server
				Response response = (Response) in.readObject();

				// Print response
				System.out.println(response);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/*
		 * Request request = null;
		 * String line = null;
		 * String[] tokens;
		 * while (sc.hasNext()) {
		 * line = sc.nextLine();
		 * tokens = line.split(" ");
		 * System.out.println(tokens[0]);
		 * if (tokens[0].equals("wallet") || tokens[0].equals("w")) {
		 * request = Request.createWalletOperation();
		 * try {
		 * out.writeObject(request);
		 * } catch (IOException e) {
		 * e.printStackTrace();
		 * }
		 * System.out.println("wallet enviado");
		 * } else if (tokens[0].equals("add") || tokens[0].equals("a")) {
		 * if (tokens.length == 3) {
		 * request = Request.createAddOperation(tokens[1], tokens[2]);
		 * try {
		 * out.writeObject(request);
		 * } catch (IOException e) {
		 * e.printStackTrace();
		 * }
		 * } else {
		 * System.out.println("Utilizacao: add <wine> <image>");
		 * }
		 * } else if (tokens[0].equals("sell") || tokens[0].equals("s")) {
		 * if (tokens.length == 4) {
		 * request = Request.createSellOperation(tokens[1], Integer.parseInt(tokens[2]),
		 * Integer.parseInt(tokens[3]));
		 * }
		 * } else if (tokens[0].equals("view") || tokens[0].equals("v")) {
		 * if (tokens.length == 2) {
		 * request = Request.createViewOperation(tokens[1]);
		 * }
		 * }
		 * if (tokens[0].equals("xau")) {
		 * break;
		 * }
		 *
		 * try {
		 * out.writeObject(request);
		 * System.out.println("mensagem enviada");
		 * } catch (IOException e) {
		 * System.out.println("erro a comunicar com o server");
		 * // TODO Auto-generated catch block
		 * // e.printStackTrace();
		 * }
		 *
		 * System.out.println("info sff...");
		 *
		 * try {
		 * int num = -1;
		 * Response response = null;
		 *
		 * if (request.operation == Request.Operation.ADD) {
		 * response = (Response) in.readObject();
		 * if (response.type == Response.Type.ERROR) {
		 * System.out.println(response.message);
		 * } else {
		 * System.out.printf("Vinho %s adicionado com sucesso", request.wine);
		 * }
		 * }
		 *
		 * if (request.operation == Request.Operation.WALLET) {
		 * System.out.println("recebi resposta do server");
		 * response = (Response) in.readObject();
		 * num = response.balance;
		 * } else {
		 * num = -1;
		 * }
		 * } catch (ClassNotFoundException e) {
		 * // TODO Auto-generated catch block
		 * // e.printStackTrace();
		 * System.out.println("nao encontro nada amigo");
		 * } catch (IOException e) {
		 * // TODO Auto-generated catch block
		 * System.out.println("io exception");
		 * e.printStackTrace();
		 * }
		 * }
		 */
	}

	private Request readRequest() {
		String line = sc.nextLine();
		String[] tokens = line.split(" ");
		String operation = tokens[0];

		/* interface CreateRequest {
			Request get(String[] tokens);
		}

		CreateRequest[] requests = new CreateRequest[] {
			new CreateRequest() { public Request get(String[] tokens) { return createAddRequest(tokens); }},
			new CreateRequest() { public Request get(String[] tokens) { return createSellRequest(tokens); }},
		};

		return requests[0].get(tokens); */

		if (operation.equals("add") || operation.equals("a")) {
			return createAddRequest(tokens);
		} else if (operation.equals("sell") || operation.equals("s")) {
			return createSellRequest(tokens);
		} else if (operation.equals("view") || operation.equals("v")) {
			return createViewRequest(tokens);
		} else if (operation.equals("buy") || operation.equals("b")) {
			return createBuyRequest(tokens);
		} else if (operation.equals("wallet") || operation.equals("w")) {
			return createWalletRequest(tokens);
		} else if (operation.equals("classify") || operation.equals("c")) {
			return createClassifyRequest(tokens);
		} else if (operation.equals("talk") || operation.equals("t")) {
			return createTalkRequest(tokens);
		} else if (operation.equals("read") || operation.equals("r")) {
			return createReadRequest(tokens);
		}
		return null;
	}

	private Request createReadRequest(String[] tokens) {
		if (tokens.length != 2) {
			System.out.println("Utilização: read");
			return null;
		}
		return Request.createReadOperation();
	}

	private Request createTalkRequest(String[] tokens) {
		if (tokens.length != 3) {
			System.out.println("Utilização: talk <user> <message>");
			return null;
		}
		String user = tokens[1];
		String message = tokens[2];
		return Request.createTalkOperation(user, message);
	}

	private Request createClassifyRequest(String[] tokens) {
		if (tokens.length != 3) {
			System.out.println("Utilização: classify <wine> <stars>");
			return null;
		}
		String wine = tokens[1];
		int stars;
		try {
			stars = Integer.parseInt(tokens[3]);
		} catch (Exception e) {
			System.out.println("Stars têm que ser inteiros");
			return null;
		}

		return Request.createClassifyOperation(wine, stars);
	}

	private Request createWalletRequest(String[] tokens) {
		if (tokens.length != 1) {
			System.out.println("Utilização: wallet");
			return null;
		}

		return Request.createWalletOperation();
	}

	private Request createBuyRequest(String[] tokens) {
		if (tokens.length != 4) {
			System.out.println("Utilização: buy <wine> <seller> <quantity>");
			return null;
		}
		String wine = tokens[1];
		String seller = tokens[2];
		int quantity;
		try {
			quantity = Integer.parseInt(tokens[3]);
		} catch (Exception e) {
			System.out.println("Quantity têm que ser inteiros");
			return null;
		}

		return Request.createBuyOperation(wine, seller, quantity);
	}

	private Request createViewRequest(String[] tokens) {
		if (tokens.length != 2) {
			System.out.println("Utilização: view <wine>");
			return null;
		}
		String wine = tokens[1];
		return Request.createViewOperation(wine);
	}

	private Request createAddRequest(String[] tokens) {
		if (tokens.length != 3) {
			System.out.println("Utilização: add <wine> <image>");
			return null;
		}
		String wine = tokens[1];
		String image = tokens[2];
		return Request.createAddOperation(wine, image);
	}

	private Request createSellRequest(String[] tokens) {
		if (tokens.length != 4) {
			System.out.println("Utilização: sell <wine> <value> <quantity>");
			return null;
		}
		String wine = tokens[1];
		int value;
		int quantity;
		try {
			value = Integer.parseInt(tokens[2]);
			quantity = Integer.parseInt(tokens[3]);
		} catch (Exception e) {
			System.out.println("Value e quantity têm que ser inteiros");
			return null;
		}

		return Request.createSellOperation(wine, value, quantity);
	}

	private static void menu() {
		System.out.println("### MENU ###");
		System.out.println("add <wine> <image>");
		System.out.println("sell <wine> <value> <quantity>");
		System.out.println("view <wine>");
		System.out.println("buy <wine> <seller> <quantity>");
		System.out.println("wallet");
		System.out.println("classify <wine> <stars>");
		System.out.println("talk <user> <message>");
		System.out.println("read");
		System.out.print("Opcão: ");
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
