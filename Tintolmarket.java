import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;

public class Tintolmarket implements Serializable {
	private static Scanner sc = new Scanner(System.in);
	private Socket clientSocket = null;
	private ObjectInputStream in = null;
	private ObjectOutputStream out = null;
	private boolean close = false;

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
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			in = new ObjectInputStream(clientSocket.getInputStream());
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
		while (!close) {

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

				if (request.operation == Request.Operation.ADD) {
					BufferedImage image = WineImage.readImageFromDisk(WineImage.getImagePath(request.image));
					WineImage.sendImage(image, out, request.image);
				}

				// Receive response from server
				Response response = (Response) in.readObject();

				if(response.type == Response.Type.VIEW){
					BufferedImage image = WineImage.readImageFromNetwork(in);
					File folder = WineImage.createFolder("client-images");
					System.out.println("before");
					WineImage.writeImageToFile(folder, image, WineImage.getImageExtension(response.image));
					System.out.println("after");
				}
				if(response.type == Response.Type.READ){
					System.out.println("MENSAGENS:");
					Set<String> senders = response.messages.keySet();
					for (String sender : senders) {
						ArrayList<String> messages = response.messages.get(sender);
						System.out.println(sender + "....");
						for (int i = 0; i < messages.size(); i++) {
							System.out.println(messages.get(i));
						}
						System.out.println();
					}
					System.out.println();
				}
				response.responseToString();
			} catch (IOException e) {
				System.out.println("imagem nao existe");
				//e.printStackTrace();
			} catch(Exception e2){
				close = true;
			}
		}
	}

	private Request readRequest() {
		String line = null;
		try {
			line = sc.nextLine();

		} catch (Exception e) {
			disconnectFromServer();
		}
		String[] tokens = line.split(" ");
		String operation = tokens[0];

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
		if (tokens.length != 1) {
			System.out.println("Utilização: read");
			return null;
		}
		return Request.createReadOperation();
	}

	private Request createTalkRequest(String[] tokens) {
		if (tokens.length < 3) {
			System.out.println("Utilização: talk <user> <message>");
			return null;
		}
		String user = tokens[1];
		String message = "";
		for(int i = 2; i < tokens.length; i++){
			message += " " + tokens[i];
		}
		return Request.createTalkOperation(user, message.trim());
	}

	private Request createClassifyRequest(String[] tokens) {
		if (tokens.length != 3) {
			System.out.println("Utilização: classify <wine> <stars>");
			return null;
		}
		String wine = tokens[1];
		int stars;
		try {
			stars = Integer.parseInt(tokens[2]);
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
		try {
			return Request.createAddOperation(wine, image);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
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
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void close() {

	}
}
