import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
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
			System.out.println(
					"Usage: java TintolmarketServer [port] <databasePassword> <keystoreFilename> <keystorePassword>");
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
			System.out.println("Server started on port " + port + "...");
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

			// Authentication
			new Authentication();

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

				if (api.addWine(requestAddWine.wine) == null) {
					return new Response(Response.Type.ERROR, new Response.Error("Esse vinho já existe!"));
				}

				return new Response(Response.Type.OK, new Response.OK("Vinho adicionado com sucesso"));
			} else if (type == Request.Type.LISTWINE) {
				Request.ListWine requestListWine = (Request.ListWine) payload;

				if (api.listWine(threadUserID, requestListWine.name,
						requestListWine.quantity, requestListWine.price) == false) {
					return new Response(Response.Type.ERROR,
							new Response.Error("Esse vinho não existe. Crie-o primeiro."));
				}

				return new Response(Response.Type.OK, new Response.OK("Vinho colocado à venda com sucesso!"));
			} else if (type == Request.Type.VIEWWINE) {
				Request.ViewWine requestViewWine = (Request.ViewWine) payload;

				ViewWine viewWine = api.getWine(requestViewWine.name);
				if (viewWine == null) {
					return new Response(Response.Type.ERROR,
							new Response.Error("Esse vinho não existe."));
				}

				return new Response(Response.Type.VIEWWINE,
						new Response.ViewWineAndListings(viewWine.wine, viewWine.listings));

			} else if (type == Request.Type.BUYWINE) {
				Request.BuyWine requestBuyWine = (Request.BuyWine) payload;
				return api.buyWine(requestBuyWine.name, requestBuyWine.quantity, requestBuyWine.seller, threadUserID);
			} else if (type == Request.Type.WALLET) {
				  Response response = null;
				  User user = api.getUser(threadUserID);
				  if (user == null) {
					response = new Response(Response.Type.ERROR,
					new Response.Error("Erro a obter saldo. user nao existe"));
				  } else {
					response = new Response(Response.Type.OK,
					new Response.OK("Saldo obtido com sucesso: " + user.balance));
					System.out.println("saldo: " + (user.balance) + " user: " + threadUserID);
				  }
				  return response;
			} else if (type == Request.Type.CLASSIFY) {
				  Response response = null;
				  Request.ClassifyWine classifyWine = (Request.ClassifyWine) request.payload;
				  if (classifyWine.stars < 1 || classifyWine.stars > 5) {
					response = new Response(Response.Type.ERROR, new Response.Error("classificacao invalida. tem de ser entre 1 e 5"));
					//TODO VER SE ESTA LINHA RESOLVE O WARNING
					//return response;
				}

				  return api.classifyWine(classifyWine.name, classifyWine.stars);
			}
				 /*
				 if (wine == null) {
					response = new Response(Response.Type.ERROR, new
					Response.Error("Esse vinho nao existe"));
				  }

				  api.classifyWine(classifyWine.name, classifyWine.stars);

				  response = new Response(Response.Type.OK, new
				  Response.OK("vinho classificado com sucesso"));
				  return response; */
				 /* } else if (type == Request.Type.TALK) {

				  boolean messageSent = Logic.sendMessage(userId, request.user,
				  request.message);

				  if (messageSent) {
				  response.type = Response.Type.OK;
				  } else {
				  response.type = Response.Type.ERROR;
				  response.message = "Recetor não existe";
				  }

				  /*} else if (type == Request.Type.READ) {
				 * response.messages = Logic.getMessages(userId);
				 *
				 * if (response.messages.size() > 0) {
				 * response.type = Response.Type.READ;
				 * } else {
				 * response.type = Response.Type.ERROR;
				 * response.message = "nao tens nada para ler";
				 * }
				 *
				 * } else if (type == Request.Type.READ) {
				 *
				 * }
				 */

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
