import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;
import java.util.UUID;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
// TODO ADICIONAR SYSTEM TIME MILIS PARA AS IMAGENS SEREM UNICAS OU FAZER UMA PASTA PARA CADA CLIENTE
// TODO verificar assinaturas de buy e sell, fazer list,
// TODO fazer operacoes para o buy


//TODO adicionar campo preco ao buy e usar hmac



// uma venda de 10 e uma linda na blockchain, posso usar o wine listings para acompanhar o processo de compras, tentar adicionar campo preco no client e fazer view antes do buy; usar hmac em vez de hash fazer list e melhorar a verificacao da blockChain
public class Tintolmarket implements Serializable {
	//private PrivateKey privateKey = null;
	private static Scanner sc = new Scanner(System.in);
	private SSLSocket clientSocket = null;
	//private SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
	SSLSocket sslsocket = null;
	private ObjectInputStream in = null;
	private ObjectOutputStream out = null;
	private boolean close = false;
	private String trustStore = null;
	private String keyStore = null;
	private String keyStorePassword = null;
	private String userId = null;
	private SecretKey secretKey = null;

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
		this.trustStore = trustStore;
		this.userId = userId;
		this.keyStore = keyStore;
		this.keyStorePassword = keyStorePassword;
		//privateKey = SecurityRSA.getPrivateKey(keyStore, keyStorePassword, userId);

		initializeServerConnection(host, port);

		new Authentication();

		secretKey = generateSecretKey();

		run();

		disconnectFromServer();

		close();
	}

	private SecretKey generateSecretKey() throws NoSuchAlgorithmException {
		KeyGenerator kg = KeyGenerator.getInstance("AES");
		kg.init(128);
		//SecretKey key = kg.generateKey();
		return kg.generateKey();
	}

	private void initializeServerConnection(String host, int port) {
		try {
			System.setProperty("javax.net.ssl.trustStore", trustStore);
			System.setProperty("javax.net.ssl.trustStorePassword", Constants.TRUSTSTORE_PASSWORD);
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
		Authentication() {
			try {
				// Send userID
				sendAuthRequest(userId);

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
				PrivateKey privateKey = SecurityRSA.getPrivateKey(keyStore, keyStorePassword, userId);
				byte[] bytesLong = ByteUtils.longToBytes(nonce);
				byte[] signedNonce = SecurityRSA.sign(bytesLong, privateKey);
				PublicKey publicKey = SecurityRSA.getPublicKey(keyStore, keyStorePassword, userId);
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

	private void run() throws Exception {
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

				} else if(response.type == Response.Type.READ){
					PrivateKey privateKey = SecurityRSA.getPrivateKey(keyStore, keyStorePassword, userId);
					Response.ReadMessages responseMessages = (Response.ReadMessages)response.payload;
					ArrayList<String> messages = responseMessages.messages;
					/* String secretKeyString = null;
					if (messages.size() > 0) {
						secretKeyString = messages.get(0).split(":")[3];
					} */
					for (int i = 0; i < messages.size(); i++) {
						String[] messageTokens = messages.get(i).split(":");
						String secretKeyString = messages.get(i).split(":")[3];
						//Key decryptedSecretKey = SecurityRSA.unwrapKey(messageTokens[3].getBytes(), privateKey);
						Key decryptedSecretKey = SecurityRSA.decryptAesKey(Base64.getDecoder().decode(secretKeyString)/* messageTokens[3].getBytes() */, privateKey);

						System.out.println(String.format("%s recebeu a seguinte mensagem de %s: %s", userId, messageTokens[1], SecurityRSA.decrypt(Base64.getDecoder().decode(messageTokens[2])/* .getBytes() */, decryptedSecretKey)));
					}
				}
			} catch (Exception e2) {
				close = true;
				e2.printStackTrace();
			}
		}
	}

	private Request readRequest() throws Exception {
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

			// Create List Wine request
			String wineName = tokens[1];
			int price = Integer.parseInt(tokens[2]);
			int quantity = Integer.parseInt(tokens[3]);
			String uuid = UUID.randomUUID().toString();
			Request.ListWine listWine = new Request.ListWine(new WineSale(uuid, wineName, price, quantity));

			// Sign request
			PrivateKey privateKey = SecurityRSA.getPrivateKey(keyStore, keyStorePassword, userId);
			Request.Signed<Request.ListWine> signedListWine = new Request.Signed<Request.ListWine>(listWine, privateKey);

			// Send request
			return new Request(Request.Type.LISTWINE, signedListWine);
		} else if (operation.equals("view") || operation.equals("v")) {
			if(tokens.length != 2){
				System.out.println("Usage: view <wine>");
			}

			return new Request(Request.Type.VIEWWINE, new Request.ViewWine(tokens[1]));
		} else if (operation.equals("buy") || operation.equals("b")) {
			if (tokens.length != 4) {
				System.out.println("Usage: buy <wine> <seller> <quantity>");
				return null;
			}

			String wineName = tokens[1];
			String seller = tokens[2];
			int quantity = Integer.parseInt(tokens[3]);

			PrivateKey privateKey = SecurityRSA.getPrivateKey(keyStore, keyStorePassword, userId);
			int sizeOfInt = 4;
			byte[] bytesLong = new byte[userId.length() + wineName.length() + sizeOfInt];
			//<userId>:<uuid>:<wineName>:<quantity>:<seller>:<base64Signature> antigo

			// <buy>:<uuid>:<wineName>:<seller>:<assinatura>

			ArrayList<String> nfts = new ArrayList<>();
			ArrayList<String> signedNfts = new ArrayList<>();

			String uuid = null;
			for (int i = 0; i < quantity; i++) {
				uuid = UUID.randomUUID().toString();
				bytesLong = copyInfoToArrayBuy("sell", uuid, wineName, userId,
						"sell".length() + uuid.length() + wineName.length() + sizeOfInt + userId.length());
				nfts.add(Base64.getEncoder().encodeToString(bytesLong));
				signedNfts.add(Base64.getEncoder().encodeToString(SecurityRSA.sign(bytesLong, privateKey)));
			}

			//bytesLong = copyInfoToArrayBuy(userId, uuid, wineName, quantity, userId.length() + wineName.length() + sizeOfInt);

			//byte[] signedTransaction = SecurityRSA.sign(bytesLong, privateKey);

			return new Request(Request.Type.BUYWINE, new Request.BuyWine(uuid, wineName, seller, quantity, signedNfts, nfts));
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
			if (tokens.length < 3) {
				System.out.println("Usage: talk <user> <message>");
				return null;
			}
			PublicKey publicKey = null;
			try {
				publicKey = SecurityRSA.getPublicKey(trustStore, "password", tokens[1]);
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("Nao foi possivel obter a chave publica");
			}
			StringBuilder messageBuilder = new StringBuilder();
			for (int i = 2; i < tokens.length; i++) {
				messageBuilder.append(tokens[i] + " ");
			}

			String completeMessage = messageBuilder.toString().trim();

			String encryptedMessage = null;

			byte[] wrapedAESKey = null;
			try {
				//encryptedMessage = SecurityRSA.encryptAES128(completeMessage, Base64.getEncoder().encodeToString(secretKey.getEncoded()));
				encryptedMessage = Base64.getEncoder().encodeToString(SecurityRSA.encryptData(completeMessage, secretKey));

				//wrapedAESKey = SecurityRSA.wrapKey(secretKey, publicKey);
				wrapedAESKey = SecurityRSA.encryptAesKey(secretKey, publicKey);
				//SecurityRSA.encryptMessage(publicKey, completeMessage);
			} catch (InvalidKeyException | IllegalBlockSizeException | NoSuchAlgorithmException | NoSuchPaddingException e) {
				System.out.println("Erro a cifrar mensagem");
				// e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new Request(Request.Type.TALK, new Request.Talk(tokens[1], encryptedMessage, wrapedAESKey));
		} else if (operation.equals("read") || operation.equals("r")) {
			return new Request(Request.Type.READ, null);
		} else if(operation.equals("list") || operation.equals("l")){
			return new Request(Request.Type.LISTWINE, null);
		}

		return null;
	}

	private byte[] copyInfoToArrayList(String operation, String uuid, String userId, int quantity, String winename, int price,  int totalLength) {
		// <sell>:<uuid>:<userId>:<quantity>:<winename>:<price>
		byte[] bytesLong = new byte[totalLength];
		byte[] quantityBytes = bigIntToByteArray(quantity);
		byte[] priceBytes = bigIntToByteArray(price);
		int length = 0;
		System.arraycopy(operation.getBytes(), 0, bytesLong, 0, operation.length());

		length+= operation.length();

		System.arraycopy(uuid.getBytes(), 0, bytesLong, totalLength, uuid.length());

		length += uuid.length();

		System.arraycopy(userId.getBytes(), 0, bytesLong, length, userId.length());

		length+= userId.length();

		System.arraycopy(quantityBytes, 0, bytesLong, length, quantityBytes.length);

		length+= quantityBytes.length;

		System.arraycopy(winename.getBytes(), 0, bytesLong, length, winename.length());

		length+= winename.length();

		System.arraycopy(priceBytes, 0, bytesLong, length, priceBytes.length);

		return bytesLong;
	}

	private byte[] copyInfoToArrayBuy(String operation, String uuid, String wineName, String seller, int length) {
		byte[] bytesLong = new byte[length];
		System.arraycopy(operation.getBytes(), 0, bytesLong, 0, operation.length());

		System.arraycopy(uuid.getBytes(), 0, bytesLong, operation.length(), uuid.length());

		System.arraycopy(wineName.getBytes(), 0, bytesLong, operation.length() + uuid.length(), wineName.length());

		System.arraycopy(seller.getBytes(), 0, bytesLong, operation.length() + uuid.length() + wineName.length(), seller.length());

		return bytesLong;
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
		System.out.println("list");
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

	private byte[] bigIntToByteArray(int i) {
		byte[] bytes = ByteBuffer.allocate(4).putInt(i).array();
		return bytes;
	}
}
