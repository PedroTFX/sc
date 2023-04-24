import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Random;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class TintolmarketServer implements Serializable {
	private static boolean close = false;
	private API api = null;

	public static void main(String[] args) {
		if (args.length != 3 && args.length != 4) {
			System.out.println("Usage: java TintolmarketServer [port] <databasePassword> <keystoreFilename> <keystorePassword>");
			return;
		}

		int index = 0;
		int port = 12345;
		if (args.length == 4) {
			port = Integer.parseInt(args[index++]);
		}

		String passwordCifra = args[index++];
		String keystore = args[index++];
		String keystorePassword = args[index];

		new TintolmarketServer(port, passwordCifra, keystore, keystorePassword);
	}

	TintolmarketServer(int port, String passwordCifra, String keystoreFilename, String keystorePassword) {
		// Initialize API
		api = new API(passwordCifra);

		try {
			System.out.println(Integrity.verifyIntegrity(Integrity.getAbsolutePath(Constants.USER_FILE), "SHA", Integrity.getAbsolutePath(Constants.USER_HASH)));
			System.out.println(Integrity.verifyIntegrity(Integrity.getAbsolutePath(Constants.WINE_FILE), "SHA", Integrity.getAbsolutePath(Constants.WINE_HASH)));
			System.out.println(Integrity.verifyIntegrity(Integrity.getAbsolutePath(Constants.WINE_LISTINGS_FILE), "SHA", Integrity.getAbsolutePath(Constants.WINE_LISTINGS_HASH)));
			System.out.println(Integrity.verifyIntegrity(Integrity.getAbsolutePath(Constants.MESSAGE_FILE), "SHA", Integrity.getAbsolutePath(Constants.MESSAGE_HASH)));
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}

		// Initialize server
		startServer(port, keystoreFilename, keystorePassword);
	}

	private void startServer(int port, String keystoreFilename, String keystorePassword) {
		// Initialize secure server socket
		SSLServerSocket sslServerSocket = initSecureServerSocket(port, keystoreFilename, keystorePassword);

		try {
			while (!close) {
				SSLSocket inSoc = (SSLSocket) sslServerSocket.accept();
				new ServerThread(inSoc);
			}
			sslServerSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private SSLServerSocket initSecureServerSocket(int port, String keystoreFilename, String keystorePassword) {
		SSLServerSocket sslServerSocket = null;
		try {
			System.setProperty("javax.net.ssl.keyStore", keystoreFilename);
			System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);
			/* SSL */ServerSocketFactory sslServerSocketFactory = /* (SSLServerSocketFactory) */ SSLServerSocketFactory
					.getDefault();
			sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
			System.out.println("Server started on port porto " + port + "...");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sslServerSocket;
	}

	// Threads utilizadas para comunicacao com os clientes
	class ServerThread extends Thread {
		private Socket socket = null;
		ObjectOutputStream outStream;
		ObjectInputStream inStream;
		String threadUserID = null;
		boolean close = false;

		ServerThread(Socket inSoc) {
			socket = inSoc;
			start();
		}

		public void run() {
			// Initialization
			initConnectionStreams();

			boolean clean = false;

			try {
				clean = Integrity.verifyIntegrity(Integrity.getAbsolutePath(Constants.USER_FILE), "SHA", Integrity.getAbsolutePath(Constants.USER_HASH));
				if(!clean){
					System.out.println("Ficheiro socratizado. A fechar thread");
					close = true;
					//return;
				} else {
					System.out.println("Auth: ficheiro dos users verificado e esta bom");
				}
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Authentication
			new Authentication();

			try {
				Integrity.updateHashValue(Integrity.getAbsolutePath(Constants.USER_FILE), "SHA", Integrity.getAbsolutePath(Constants.USER_HASH));
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Listen to requests
			listen();

			// Close socket and thread
			closeServerThread();
		}

		private void initConnectionStreams() {
			try {
				inStream = new ObjectInputStream(socket.getInputStream());
				outStream = new ObjectOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		class Authentication {
			String authenticationUserID;
			long nonce;

			Authentication() {
				try {
					// Receive authentication request (and get authenticationUserID)
					receiveAuthRequest();

					// Reply with nonce and flag that indicates if user ID exists
					nonce = new Random().nextLong();
					User newUser = api.getUser(authenticationUserID);// Data.readUserInfoFromFile(authenticationUserID)
					outStream.writeObject(
							new Response(Response.Type.AUTHNONCE, new Response.AuthNonce(nonce, newUser == null)));

					// Register or login user
					Response response = null;
					if (newUser == null) {
						response = register();
					} else {
						response = login();
					}

					outStream.writeObject(response);

					// Everything OK: save userID in thread
					threadUserID = authenticationUserID;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			void receiveAuthRequest() throws Exception {
				Request request = (Request) inStream.readObject();
				Request.Type type = request.type;
				Object payload = request.payload;

				// Check if operation is AUTH USER ID
				if (type != Request.Type.AUTHUSERID) {
					outStream.writeObject(new Response(Response.Type.ERROR, new Response.Error(
							"O utilizador tem que se autenticar primeiro!")));
					return;
				}

				authenticationUserID = ((Request.AuthUserID) payload).userID;
			}

			Response register() throws Exception {
				// Read request
				Request request = (Request) inStream.readObject();

				// Check if it's a registration request
				if (request.type != Request.Type.AUTHREGISTER) {
					close = true;
					return new Response(Response.Type.ERROR,
							new Response.Error("You need to send an AUTHREGISTER operation!"));
				}

				// Get payload
				Request.AuthRegister requestAuth = (Request.AuthRegister) request.payload;
				long clientNonce = requestAuth.nonce;
				byte[] signedNonce = requestAuth.signedNonce;
				PublicKey publicKey = requestAuth.key;

				// Check nonce
				if (!isValidNonce(clientNonce)) {
					close = true;
					return new Response(Response.Type.ERROR, new Response.Error(
							"You need to send the same nonce back!"));
				}

				// Check signed nonce
				if (!SecurityRSA.isSignedNonce(ByteUtils.longToByteArray(clientNonce), signedNonce, publicKey)) {
					close = true;
					return new Response(Response.Type.ERROR, new Response.Error(
							"Nonce has the wrong signature!"));
				}

				// Register user
				User user = new User(authenticationUserID, publicKey);
				User addedUser = api.addUser(user);
				if (addedUser == null) {
					return new Response(Response.Type.ERROR, new Response.Error(
							"Failed to create new user!"));
				}

				return new Response(Response.Type.OK,
						new Response.OK(String.format("%s successfully registered!", authenticationUserID)));
			}

			boolean isValidNonce(long clientNonce) {
				return nonce == clientNonce;
			}

			Response login() throws Exception {
				// Read request
				Request request = (Request) inStream.readObject();

				// Check if it's a registration request
				if (request.type != Request.Type.AUTHLOGIN) {
					close = true;
					return new Response(Response.Type.ERROR,
							new Response.Error("You need to send an AUTHLOGIN operation!"));
				}

				Request.AuthLogin requestAuthLogin = (Request.AuthLogin) request.payload;
				long clientNonce = requestAuthLogin.nonce;
				byte[] signedNonce = requestAuthLogin.signedNonce;

				// Check nonce
				if (!isValidNonce(clientNonce)) {
					return new Response(Response.Type.ERROR,
							new Response.Error("You need to send the same nonce back!"));
				}

				// Get user's public key and check if the nonce was correctly signed
				User user;
				try {
					user = api.getUser(authenticationUserID);
				} catch (Exception e) {
					return new Response(Response.Type.ERROR,
							new Response.Error("Error getting this user from the database!"));
				}
				if (!SecurityRSA.isSignedNonce(ByteUtils.longToByteArray(clientNonce), signedNonce, user.key)) {
					return new Response(Response.Type.ERROR,
							new Response.Error("Nonce has the wrong signature!"));
				}

				return new Response(Response.Type.OK,
						new Response.OK(String.format("%s successfully logged in!", authenticationUserID)));
			}
		}

		private void listen() {
			System.out.printf("Listening to client %s requests...\n", threadUserID);
			while (!socket.isClosed() && !close) {
				try {
					Request request = (Request) inStream.readObject();
					Response response = processRequest(request);
					outStream.writeObject(response);
				} catch (Exception e1) {
					System.out.println("Houve uma excepção no cliente, vamos fechar a ligação...");
					close = true;
				}
			}
		}

		private Response processRequest(Request request) throws Exception {
			Request.Type type = request.type;
			Object payload = request.payload;

			if (type == Request.Type.ADDWINE) {
				Request.AddWine requestAddWine = (Request.AddWine) payload;

				boolean winesIntegrity = Integrity.verifyIntegrity(Integrity.getAbsolutePath(Constants.WINE_FILE), "SHA", Integrity.getAbsolutePath(Constants.WINE_HASH));
				System.out.println("A verificar a integridade do ficheiro dos vinhos antes do pedido add: " + winesIntegrity);

				if (!winesIntegrity) {

					return new Response(Response.Type.ERROR, new Response.Error("operacao abortada. ficheiro corrompido"));
				} else{

					if (api.addWine(requestAddWine.wine) == null) {
						return new Response(Response.Type.ERROR, new Response.Error("Esse vinho já existe!"));
					}

					Integrity.updateHashValue(Integrity.getAbsolutePath(Constants.WINE_FILE), "SHA", Integrity.getAbsolutePath(Constants.WINE_HASH));
					System.out.println("Hash atualizado nos vinhos");

					return new Response(Response.Type.OK, new Response.OK("Vinho adicionado com sucesso"));
				}
			} else if (type == Request.Type.LISTWINE) {
				Request.ListWine requestListWine = (Request.ListWine) payload;

				boolean ListIntegrity = Integrity.verifyIntegrity(Integrity.getAbsolutePath(Constants.WINE_LISTINGS_FILE), "SHA", Integrity.getAbsolutePath(Constants.WINE_LISTINGS_HASH));
				System.out.println("A verificar a integridade do ficheiro das vendas antes do pedido listWine: " + ListIntegrity);

				if (!ListIntegrity) {
					return new Response(Response.Type.ERROR, new Response.Error("operacao abortada. ficheiro corrompido"));
				} else {

					if (api.listWine(threadUserID, requestListWine.name, requestListWine.quantity, requestListWine.price) == false) {
						return new Response(Response.Type.ERROR, new Response.Error("Esse vinho não existe. Crie-o primeiro."));
					}

					Integrity.updateHashValue(Integrity.getAbsolutePath(Constants.WINE_LISTINGS_FILE), "SHA", Integrity.getAbsolutePath(Constants.WINE_LISTINGS_HASH));
					System.out.println("Hash atualizado nas vendas");
					return new Response(Response.Type.OK, new Response.OK("Vinho colocado à venda com sucesso!"));
				}
			} else if (type == Request.Type.VIEWWINE) {
				Request.ViewWine requestViewWine = (Request.ViewWine) payload;

				boolean winesIntegrity = Integrity.verifyIntegrity(Integrity.getAbsolutePath(Constants.WINE_FILE), "SHA", Integrity.getAbsolutePath(Constants.WINE_HASH));
				System.out.println("A verificar a integridade do ficheiro dos vinhos antes do pedido view: " + winesIntegrity);
				boolean ListIntegrity = Integrity.verifyIntegrity( Integrity.getAbsolutePath(Constants.WINE_LISTINGS_FILE), "SHA", Integrity.getAbsolutePath(Constants.WINE_LISTINGS_HASH));
				System.out.println("A verificar a integridade do ficheiro das vendas antes do pedido listWine: " + ListIntegrity);


				if (!winesIntegrity || !ListIntegrity) {
					return new Response(Response.Type.ERROR, new Response.Error("operacao abortada. ficheiro corrompido"));
				} else {

					ViewWine viewWine = api.getWine(requestViewWine.name);
					if (viewWine == null) {
						return new Response(Response.Type.ERROR, new Response.Error("Esse vinho não existe."));
					}

					Integrity.updateHashValue(Integrity.getAbsolutePath(Constants.WINE_FILE), "SHA", Integrity.getAbsolutePath(Constants.WINE_HASH));
					System.out.println("Hash atualizado nos vinhos");

					Integrity.updateHashValue(Integrity.getAbsolutePath(Constants.WINE_LISTINGS_FILE), "SHA", Integrity.getAbsolutePath(Constants.WINE_LISTINGS_HASH));
					System.out.println("Hash atualizado nas vendas");

					return new Response(Response.Type.VIEWWINE, new Response.ViewWineAndListings(viewWine.wine, viewWine.listings));
				}
			} else if (type == Request.Type.BUYWINE) {
				Response response = null;
				Request.BuyWine requestBuyWine = (Request.BuyWine) payload;

				boolean userIntegrity = Integrity.verifyIntegrity(Integrity.getAbsolutePath(Constants.USER_FILE), "SHA", Integrity.getAbsolutePath(Constants.USER_HASH));
				boolean wineIntegrity = Integrity.verifyIntegrity(Integrity.getAbsolutePath(Constants.WINE_FILE), "SHA", Integrity.getAbsolutePath(Constants.WINE_HASH));
				boolean listIntegrity = Integrity.verifyIntegrity(Integrity.getAbsolutePath(Constants.WINE_LISTINGS_FILE), "SHA", Integrity.getAbsolutePath(Constants.WINE_LISTINGS_HASH));

				if (!userIntegrity || !wineIntegrity || !listIntegrity) {
					return new Response(Response.Type.ERROR, new Response.Error("operacao abortada. ficheiro corrompido"));
				} else {

					response = api.buyWine(requestBuyWine.name, requestBuyWine.quantity, requestBuyWine.seller, threadUserID);

					Integrity.updateHashValue(Integrity.getAbsolutePath(Constants.USER_FILE), "SHA", Integrity.getAbsolutePath(Constants.USER_HASH));
					System.out.println("Hash atualizado nos users");

					Integrity.updateHashValue(Integrity.getAbsolutePath(Constants.WINE_FILE), "SHA", Integrity.getAbsolutePath(Constants.WINE_HASH));
					System.out.println("Hash atualizado nos vinhos");

					Integrity.updateHashValue(Integrity.getAbsolutePath(Constants.WINE_LISTINGS_FILE), "SHA", Integrity.getAbsolutePath(Constants.WINE_LISTINGS_HASH));
					System.out.println("Hash atualizado nas vendas");

					return response;
				}
			} else if (type == Request.Type.WALLET) {

				boolean userIntegrity = Integrity.verifyIntegrity(Integrity.getAbsolutePath(Constants.USER_FILE), "SHA", Integrity.getAbsolutePath(Constants.USER_HASH));

				if (!userIntegrity) {
					return new Response(Response.Type.ERROR, new Response.Error("operacao abortada. ficheiro corrompido"));
				} else {

					Response response = null;
					User user = api.getUser(threadUserID);
					if (user == null) {
						response = new Response(Response.Type.ERROR, new Response.Error("Erro a obter saldo. user nao existe"));
					} else {
						response = new Response(Response.Type.OK, new Response.OK("Saldo obtido com sucesso: " + user.balance));
						System.out.println("saldo: " + (user.balance) + " user: " + threadUserID);
					}

					Integrity.updateHashValue(Integrity.getAbsolutePath(Constants.USER_FILE), "SHA", Integrity.getAbsolutePath(Constants.USER_HASH));
					System.out.println("Hash atualizado nos users");
					return response;
				}
			} else if (type == Request.Type.CLASSIFY) {
				Request.ClassifyWine classifyWine = (Request.ClassifyWine) request.payload;

				Response response = null;
				boolean wineIntegrity = Integrity.verifyIntegrity(Integrity.getAbsolutePath(Constants.WINE_FILE), "SHA", Integrity.getAbsolutePath(Constants.WINE_HASH));

				if (!wineIntegrity) {
					return new Response(Response.Type.ERROR, new Response.Error("operacao abortada. ficheiro corrompido"));
				} else {

					if (classifyWine.stars < 1 || classifyWine.stars > 5) {
						return new Response(Response.Type.ERROR,
						new Response.Error("classificacao invalida. tem de ser entre 1 e 5"));
					}

					response = api.classifyWine(classifyWine.name, classifyWine.stars);
					Integrity.updateHashValue(Integrity.getAbsolutePath(Constants.WINE_FILE), "SHA", Integrity.getAbsolutePath(Constants.WINE_HASH));
					System.out.println("Hash atualizado nos vinhos");

					return response;
				}

			} else if (type == Request.Type.TALK) {
				Request.Talk talk = (Request.Talk) request.payload;
				Response response = null;
				boolean wineIntegrity = Integrity.verifyIntegrity(Integrity.getAbsolutePath(Constants.MESSAGE_FILE), "SHA", Integrity.getAbsolutePath(Constants.MESSAGE_HASH));

				if (!wineIntegrity) {
					return new Response(Response.Type.ERROR, new Response.Error("operacao abortada. ficheiro corrompido"));
				} else {

					response = api.talk(talk.user, talk.message, threadUserID, talk.encryptedKey);

					Integrity.updateHashValue(Integrity.getAbsolutePath(Constants.MESSAGE_FILE), "SHA", Integrity.getAbsolutePath(Constants.MESSAGE_HASH));
					System.out.println("Hash atualizado nas mensagens");

					return response;//api.talk(talk.user, talk.message, threadUserID, talk.encryptedKey);
				}
			} else if (type == Request.Type.READ) {
				Response response = null;
				boolean wineIntegrity = Integrity.verifyIntegrity(Integrity.getAbsolutePath(Constants.MESSAGE_FILE), "SHA", Integrity.getAbsolutePath(Constants.MESSAGE_HASH));

				if (!wineIntegrity) {
					return new Response(Response.Type.ERROR, new Response.Error("operacao abortada. ficheiro corrompido"));
				} else {

					response = api.read(threadUserID);

					Integrity.updateHashValue(Integrity.getAbsolutePath(Constants.MESSAGE_FILE), "SHA", Integrity.getAbsolutePath(Constants.MESSAGE_HASH));
					System.out.println("Hash atualizado nas mensagens");

					return response;//api.read(threadUserID);
				}

			} else if (type == Request.Type.TRANSACTIONS) {
				return new Response(Response.Type.ERROR, new Response.Error("UNIMPLEMENTED"));
			}

			return new Response(Response.Type.ERROR, new Response.Error("UNIMPLEMENTED"));
		}

		private void closeServerThread() {
			try {
				System.out.println("closing thread for " + threadUserID);
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
