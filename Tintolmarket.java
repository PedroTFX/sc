import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Scanner;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Tintolmarket implements Serializable {
	private PrivateKey privateKey = null;
	private static Scanner sc = new Scanner(System.in);
	private SSLSocket clientSocket = null;
	//private SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
	SSLSocket sslsocket = null;
	private ObjectInputStream in = null;
	private ObjectOutputStream out = null;
	private boolean close = false;
	//private BufferedImage bfimage = null;

	public static void main(String[] args) throws Exception {
		// Verificar se temos pelo menos 2 argumentos
		if (args.length != 5) {
			System.out.println(
					"Utilizacao: java Tintolmarket <serverAddress[:port>] <truststore> <keystore> <password-keystore> <userID>");
			return;
		}

		// Obter endereço e porta do servidor
		String[] serverAddressPort = args[0].split(":");
		String serverAddress = serverAddressPort[0];
		int serverPort = serverAddressPort.length > 1 ? Integer.parseInt(serverAddressPort[1]) : 12345;

		String trustStore = args[1];
		String keyStore = args[2];
		String keyStorePassword = args[3];
		String userId = args[4];
		createFolder("client-images");

		// Lançar
		new Tintolmarket(serverAddress, serverPort, trustStore, keyStore, keyStorePassword, userId);
	}

	private Tintolmarket(String host, int port, String trustStore, String keyStore, String keyStorePassword, String userId) throws Exception {
		privateKey = SecurityRSA.getPrivateKey(keyStore, keyStorePassword, userId);

		initializeServerConnection(host, port, trustStore);

		new Authentication(keyStore, keyStorePassword, userId);

		run();

		disconnectFromServer();

		close();
	}

	private void initializeServerConnection(String host, int port, String trustStoreFilename) {
		try {
			System.setProperty("javax.net.ssl.trustStore", trustStoreFilename);
			System.setProperty("javax.net.ssl.trustStorePassword", "password");
			/* SocketFactory sf = SSLSocketFactory.getDefault();
			clientSocket = (SSLSocket) sf.createSocket(host, port); */
			/* SSL*/SocketFactory sslSocketFactory = /* (SSLSocketFactory) */ SSLSocketFactory.getDefault();
			clientSocket = (SSLSocket) sslSocketFactory.createSocket(host, port);
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			in = new ObjectInputStream(clientSocket.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class Authentication {
		Authentication(String keyStore, String keyStorePassword, String userID) {
			try {
				// Send userID
				sendAuthRequest(userID);

				// Receive response
				Response response = (Response) in.readObject();

				// Check if response is Auth Nonce
				if (response.type != Response.Type.AUTHNONCE) {
					out.writeObject(new Response(Response.Type.ERROR,
							new Response.Error("Server did not send a nonce response!")));
					return;
				}

				// Extract data from response
				Response.AuthNonce responseAuthNonce = (Response.AuthNonce) response.payload;
				long nonce = responseAuthNonce.nonce;
				boolean newUser = responseAuthNonce.newUser;

				// If we're a new user, register, otherwise login
				PrivateKey privateKey = SecurityRSA.getPrivateKey(keyStore, keyStorePassword, userID);
				byte[] bytesLong = ByteUtils.longToBytes(nonce);
				byte[] signedNonce = SecurityRSA.sign(bytesLong, privateKey);
				PublicKey publicKey = SecurityRSA.getPublicKey(keyStore, keyStorePassword, userID);
				Request request = newUser
						? new Request(Request.Type.AUTHREGISTER, new Request.AuthRegister(nonce, signedNonce,
								publicKey))
						: new Request(Request.Type.AUTHLOGIN, new Request.AuthLogin(nonce, signedNonce));
				out.writeObject(request);

				// Wait for confirmation
				Response confirmationResponse = (Response) in.readObject();

				// Check if there was an error
				if (confirmationResponse.type != Response.Type.OK) {
					Response.Error error = (Response.Error) confirmationResponse.payload;
					System.out.println("Error: " + error.message);
					System.exit(-1);
				}

				// Everything ok: print succcess message
				Response.OK ok = (Response.OK) confirmationResponse.payload;
				System.out.println(ok.message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void sendAuthRequest(String userID) throws IOException {
			Request request = new Request(Request.Type.AUTHUSERID, new Request.AuthUserID(userID));
			out.writeObject(request);
		}
	}

	private void run() throws IOException {
		while (!close) {

			Request request;
			do {
				menu();
				request = readRequest();
			} while (request == null);

			try {
				out.writeObject(request);

				Response response = (Response) in.readObject();

				if(response.type == Response.Type.ERROR) {
					System.out.println(((Response.Error) response.payload).message);
				} else if (response.type == Response.Type.OK) {
					System.out.println(((Response.OK) response.payload).message);
				} else if(response.type == Response.Type.VIEWWINE) {
					Response.ViewWineAndListings viewWineAndListings = (Response.ViewWineAndListings) response.payload;
					Wine wine = viewWineAndListings.wine;

					// Show wine info
					double avg = wine.averageEvaluation();
					String averageEvaluation = avg == -1 ? "no evaluations yet" : String.format("%.2f stars", avg);
					String line = String.format("%s (%s)", wine.name, averageEvaluation);
					System.out.println(line);

					// Save wine image
					ImageUtils.base64ToFile(Constants.CLIENT_IMAGES_FOLDER, wine.name, wine.base64Image, wine.extension);

					// Show listings
					ArrayList<Listing> listings = viewWineAndListings.listings;
					System.out.println("LISTINGS:");
					for (Listing listing : listings) {
						System.out.println(String.format("%s is selling %d bottles of %s at %d€.", listing.seller, listing.quantity, listing.name, listing.price));
					}

				}
			} catch (Exception e2) {
				close = true;
				e2.printStackTrace();
			}
		}
	}

	private Request readRequest() throws IOException {
		String line = null;
		try {
			line = sc.nextLine();
		} catch (Exception e) {
			disconnectFromServer();
		}

		String[] tokens = line.split(" ");
		String operation = tokens[0];

		if (operation.equals("add") || operation.equals("a")) {
			// Check arguments
			if (tokens.length != 3) {
				System.out.println("Usage: add <wine> <image>");
				return null;
			}

			// Extract arguments
			String wine = tokens[1];
			String imageName = tokens[2];
			String[] imageNameTokens = imageName.split("\\.");
			String extension = imageNameTokens[imageNameTokens.length - 1];

			// Read image file into base64 string
			String base64Image = ImageUtils.fileToBase64("", imageName);

			return new Request(Request.Type.ADDWINE, new Request.AddWine(new Wine(wine, base64Image, extension)));
		} else if (operation.equals("sell") || operation.equals("s")) {
			if (tokens.length != 4) {
				System.out.println("Usage: sell <wine> <value> <quantity>");
				return null;
			}

			String wineName = tokens[1];
			int value = Integer.parseInt(tokens[2]);
			int quantity = Integer.parseInt(tokens[3]);

			return new Request(Request.Type.LISTWINE, new Request.ListWine(wineName, value, quantity));
		} else if (operation.equals("view") || operation.equals("v")) {
			if(tokens.length != 2){
				System.out.println("Usage: view <wine>");
			}

			return new Request(Request.Type.VIEWWINE, new Request.ViewWine(tokens[1]));
		} else if (operation.equals("buy") || operation.equals("b")) {
			if (tokens.length != 4) {
				System.out.println("Usage: buy <wine> <seller> <quantity>");
			}

			String wineName = tokens[1];
			String seller = tokens[2];
			int quantity = Integer.parseInt(tokens[3]);

			return new Request(Request.Type.BUYWINE, new Request.BuyWine(wineName, seller, quantity));
		} else if (operation.equals("wallet") || operation.equals("w")) {
			return new Request(Request.Type.WALLET, null);
		} else if (operation.equals("classify") || operation.equals("c")) {
			// Check arguments
			if (tokens.length != 3) {
				System.out.println("Usage: classify <wine> <stars>");
				return null;
			}

			return new Request(Request.Type.CLASSIFY, new Request.ClassifyWine(tokens[1], Integer.parseInt(tokens[2])));

		} else if (operation.equals("talk") || operation.equals("t")) {
			if (tokens.length != 3) {
				System.out.println("Usage: talk <user> <message>");
				return null;
			}

			return new Request(Request.Type.TALK, new Request.Talk(tokens[0], tokens[1]));
		} else if (operation.equals("read") || operation.equals("r")) {
			return new Request(Request.Type.READ, null);
		} else if(operation.equals("list") || operation.equals("l")){
			return new Request(Request.Type.LISTWINE, null);
		}

		return null;
	}
	/*
	 * private Request createReadRequest(String[] tokens) {
	 * if (tokens.length != 1) {
	 * System.out.println("Utilização: read");
	 * return null;
	 * }
	 * return Request.createReadOperation();
	 * }
	 *
	 * private Request createTalkRequest(String[] tokens) {
	 * if (tokens.length < 3) {
	 * System.out.println("Utilização: talk <user> <message>");
	 * return null;
	 * }
	 * String user = tokens[1];
	 * String message = "";
	 * for (int i = 2; i < tokens.length; i++) {
	 * message += " " + tokens[i];
	 * }
	 * return Request.createTalkOperation(user, message.trim());
	 * }
	 *
	 * private Request createClassifyRequest(String[] tokens) {
	 * if (tokens.length != 3) {
	 * System.out.println("Utilização: classify <wine> <stars>");
	 * return null;
	 * }
	 * String wine = tokens[1];
	 * int stars;
	 * try {
	 * stars = Integer.parseInt(tokens[2]);
	 * } catch (Exception e) {
	 * System.out.println("Stars têm que ser inteiros");
	 * return null;
	 * }
	 *
	 * return Request.createClassifyOperation(wine, stars);
	 * }
	 *
	 * private Request createWalletRequest(String[] tokens) {
	 * if (tokens.length != 1) {
	 * System.out.println("Utilização: wallet");
	 * return null;
	 * }
	 *
	 * return Request.createWalletOperation();
	 * }
	 *
	 * private Request createBuyRequest(String[] tokens) {
	 * if (tokens.length != 4) {
	 * System.out.println("Utilização: buy <wine> <seller> <quantity>");
	 * return null;
	 * }
	 * String wine = tokens[1];
	 * String seller = tokens[2];
	 * int quantity;
	 * try {
	 * quantity = Integer.parseInt(tokens[3]);
	 * } catch (Exception e) {
	 * System.out.println("Quantity têm que ser inteiros");
	 * return null;
	 * }
	 *
	 * return Request.createBuyOperation(wine, seller, quantity);
	 * }
	 *
	 * private Request createViewRequest(String[] tokens) {
	 * if (tokens.length != 2) {
	 * System.out.println("Utilização: view <wine>");
	 * return null;
	 * }
	 * String wine = tokens[1];
	 * return Request.createViewOperation(wine);
	 * }
	 *
	 * private Request createAddRequest(String[] tokens) {
	 * if (tokens.length != 3) {
	 * System.out.println("Utilização: add <wine> <image>");
	 * return null;
	 * }
	 * String wine = tokens[1];
	 * String image = tokens[2];
	 * // Check if image exists
	 * try {
	 * bfimage = WineImage.readImageFromDisk(WineImage.getImagePath(image));
	 * } catch (IOException e) {
	 * System.out.println("imagem nao existe");
	 * return null;
	 * }
	 * try {
	 * return Request.createAddOperation(wine, image);
	 * } catch (IOException e) {
	 * e.printStackTrace();
	 * }
	 * return null;
	 * }
	 *
	 * private Request createSellRequest(String[] tokens) {
	 * if (tokens.length != 4) {
	 * System.out.println("Utilização: sell <wine> <value> <quantity>");
	 * return null;
	 * }
	 * String wine = tokens[1];
	 * int value;
	 * int quantity;
	 * try {
	 * value = Integer.parseInt(tokens[2]);
	 * quantity = Integer.parseInt(tokens[3]);
	 * } catch (Exception e) {
	 * System.out.println("Value e quantity têm que ser inteiros");
	 * return null;
	 * }
	 *
	 * return Request.createSellOperation(wine, value, quantity);
	 * }
	 */

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

	public static Boolean createFolder(String name) {
		// Create a folder for the images
		File folder = new File(name);
		if (!folder.exists()) {
			folder.mkdir();
			System.out.println("Created folder: " + name);
		}
		return folder.exists();
	}
}
