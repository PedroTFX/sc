import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class TintolmarketServer implements Serializable {
	private static int port = 12345; // Default port
	private static boolean close = false;

	public static void main(String[] args) {
		if (args.length != 3 && args.length != 4) {
			System.out.println(
					"Usage: java TintolmarketServer [port] <databasePassword> <keystoreFilename> <keystorePassword>");
			return;
		}

		int index = 0;
		if (args.length == 4) {
			port = Integer.parseInt(args[index++]);
		}
		String password_cifra = args[index++];
		String keystore = args[index++];
		String keystorePassword = args[index];

		new TintolmarketServer().startServer(port, password_cifra, keystore, keystorePassword);
	}

	/**
	 * Starts the server on the specified port
	 *
	 * @param port
	 */
	public void startServer(int port, String password_cifra, String keystoreFilename, String keystorePassword) {
		// Initialize secure server socket
		SSLServerSocket sslServerSocket = getSecureSocket(keystoreFilename, keystorePassword);

		// Initialize database
		Data.createDBs();

		// Launch threads for new client requests
		while (!close) {
			try {
				// Listen for client connections
				SSLSocket inSoc = (SSLSocket) sslServerSocket.accept();
				// System.out.println("Client connected: " +
				// sslListener.getInetAddress().getHostName());
				new ServerThread(inSoc);
			} catch (/* IO */Exception e) {
				e.printStackTrace();
			}
		}

		// Close
		try {
			sslServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private SSLServerSocket getSecureSocket(String keystoreFilename, String keystorePassword) {
		SSLServerSocket sslServerSocket = null;
		try {
			//System.setProperty("javax.net.ssl.keyStoreType", "JCEKS");
			System.setProperty("javax.net.ssl.keyStore", keystoreFilename);
			System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);
			//System.setProperty("javax.net.debug", "ssl");

			try {
				TintolmarketServer.class.getResource(keystoreFilename).getFile();
			} catch (Exception e) {
				e.printStackTrace();
			}

			String trustStore = System.getProperty("javax.net.ssl.keyStore");
			String storeLoc = System.getProperty("java.class.path");
			System.out.println("classpath: " + storeLoc);
			System.out.println("Key store location: " + trustStore);

			SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
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
		String userId = "joao"; //TODO TU TROCAME ISTO APH
		boolean close = false;

		ServerThread(Socket inSoc) {
			socket = inSoc;
			start();
		}

		public void run() {
			// Initialization
			init();
			System.out.println("Thread inicializada");

			// Authentication
			//authenticate();
			System.out.printf("Utilizador %s autenticado\n", userId);

			// Listen to requests
			listen();

			// Close socket and thread
			closeServerThread();
		}

		private void init() {
			try {
				inStream = new ObjectInputStream(socket.getInputStream());
				outStream = new ObjectOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void authenticate() {
			try {
				// init
				Request request = (Request) inStream.readObject(); // Read request
				request.requestToString();
				Response response = new Response(); // Prepare response

				// Check if request is an authentication request
				if (request.operation != Request.Operation.AUTHENTICATE) {
					response.type = Response.Type.ERROR;
					response.message = "O utilizador tem que se autenticar primeiro!";
					outStream.writeObject(response);
					close = true;
				}

				if (Data.confirmPassword(request.user, request.password)) {
					userId = request.user;
					response.type = Response.Type.OK;
					response.message = "OK";
					// Send response
					outStream.writeObject(response);
				} else {
					response.type = Response.Type.ERROR;
					response.message = "Combinação userID/password incorreta";
					outStream.writeObject(response);
					close = true;
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		private void listen() {
			System.out.printf("Listening to client %s requests...\n", userId);
			while (!socket.isClosed() && !close) {
				try {
					Request request = (Request) inStream.readObject();
					Response response = processRequest(request);
					outStream.writeObject(response);

					if (response.type == Response.Type.VIEW) {
						// Ir à DB ler o nome da imagem deste vinho no disco
						String wineImageName = Data.readImageNameFromWineImageFile(request.wine);
						// Ler imagem do disco
						BufferedImage image = WineImage
								.readImageFromDisk(WineImage.getImagePath(Constants.IMAGES_FOLDER + wineImageName));
						// Enviar imagem pela rede
						WineImage.sendImage(image, outStream, wineImageName);
					}
				} catch (Exception e1) {
					System.out.println("Houve uma excepção no cliente, vamos fechar a ligação...");
					close = true;
				}
			}
		}

		private Response processRequest(Request request) throws Exception {
			request.requestToString();
			Response response = new Response();
			if (request.operation == Request.Operation.ADD) {
				// Read image from network
				BufferedImage image = WineImage.readImageFromNetwork(inStream);

				// Check if wine already exists

				boolean alreadyExists = Logic.addWine(request.wine, userId);

				if (!alreadyExists) {
					response.type = Response.Type.ERROR;
					response.message = "Esse vinho já existe!";
					return response;
				} else {

					// Everything OK, save image to disk
					File folder = WineImage.createFolder();
					String[] imageTokens = request.image.split("\\.");
					String extension = imageTokens[imageTokens.length - 1];
					String wineImageName = WineImage.writeImageToFile(folder, image, extension);
					String updatedLine = request.wine + ":" + wineImageName;

					// Save wine name and image name to Database
					Data.updateImageWineFile(null, updatedLine);
				}
				// Create response
				response.type = Response.Type.OK;
			} else if (request.operation == Request.Operation.SELL) {
				boolean exists = Logic.sellWine(userId, request.wine, request.quantity, request.value);
				if (!exists) {
					response.type = Response.Type.ERROR;
					response.message = "Esse vinho não existe!";
				} else {
					response.type = Response.Type.OK;
				}
			} else if (request.operation == Request.Operation.VIEW) {
				String exists = Logic.viewWine(request.wine);
				String[] existsTokens;
				String wineImageNameToSend = null;
				if (exists == null) {
					response.type = Response.Type.ERROR;
					response.message = "Este vinho nao existe";
				}

				String wineInfo = Data.readWineInfoFromFile(request.wine);
				if (wineInfo != null) {
					String[] wineInfoTokens = wineInfo.split(":");
					existsTokens = exists.split(":");
					wineImageNameToSend = Data.readImageNameFromWineImageFile(wineInfoTokens[0]);
					response.type = Response.Type.VIEW;
					response.seller = wineInfoTokens[1];
					response.wine = request.wine;
					response.image = wineImageNameToSend;
					if (wineInfoTokens.length > 3) {
						response.averageWineClassification = Double.parseDouble(wineInfoTokens[3]);
					}
				}

			} else if (request.operation == Request.Operation.WALLET) {
				response.type = Response.Type.OK;
				if (Logic.wallet(userId) != null) {
					response.balance = Integer.parseInt(Logic.wallet(userId));
				} else {
					response.type = Response.Type.ERROR;
					response.balance = -1;
				}
			} else if (request.operation == Request.Operation.CLASSIFY) {
				if (request.stars < 1 || request.stars > 5) {
					response.type = Response.Type.ERROR;
					response.message = "classificacao invalida. tem de ser entre 1 e 5";
				}
				boolean reviewd = false;
				try {
					reviewd = Logic.classify(request.wine, request.stars);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (reviewd) {
					response.type = Response.Type.OK;
				}
			} else if (request.operation == Request.Operation.TALK) {

				boolean messageSent = Logic.sendMessage(userId, request.user, request.message);

				if (messageSent) {
					response.type = Response.Type.OK;
				} else {
					response.type = Response.Type.ERROR;
					response.message = "Recetor não existe";
				}

			} else if (request.operation == Request.Operation.READ) {
				response.messages = Logic.getMessages(userId);

				if (response.messages.size() > 0) {
					response.type = Response.Type.READ;
				} else {
					response.type = Response.Type.ERROR;
					response.message = "nao tens nada para ler";
				}

			} else if (request.operation == Request.Operation.BUY) {
				String wine = request.wine;

				// Se este vinho não estiver à venda
				String sellInfo = Data.readSellInfo(request.wine, request.seller);
				if (sellInfo == null) {
					response.type = Response.Type.ERROR;
					response.message = "Nao ha esse vinho a venda";
					return response;
				}

				// Extrair dados
				String[] sellInfoTokens = sellInfo.split(":");
				String buyer = userId;
				String seller = sellInfoTokens[1];
				int orderQuantity = request.quantity;
				int stock = Integer.parseInt(sellInfoTokens[2]);
				int price = Integer.parseInt(sellInfoTokens[3]);
				int buyerBalance = Integer.parseInt(Data.readUserInfoFromFile(userId).split(":")[2]);
				int sellerBalance = Integer.parseInt(Data.readUserInfoFromFile(seller).split(":")[2]);

				// Calcular stock atualizada
				int newStock = stock - orderQuantity;

				// Calcular saldo do vendedor atualizado
				int newSellerBalance = sellerBalance + orderQuantity * price;

				// Calcular saldo do comprador atualizado
				int newBuyerBalance = buyerBalance - orderQuantity * price;

				// Se não houverem vinhos suficientes
				if (newStock < 0) {
					response.type = Response.Type.ERROR;
					response.message = "Nao ha quantidade suficiente";
					return response;
				}

				// Se o compardor não tiver saldo suficiente
				if (newBuyerBalance < 0) {
					response.type = Response.Type.ERROR;
					response.message = "Nao ha saldo suficiente";
					return response;
				}

				// Autalizar stock de vinho na dase de dados
				Data.updateWineStock(wine, userId, newStock, seller);

				// Atualizar saldo do vendedor na dase de dados
				Data.updateUserBalance(seller, newSellerBalance);

				// Atualizar saldo do comprador na dase de dados
				Data.updateUserBalance(userId, newBuyerBalance);

				// Construir resposta
				response.type = Response.Type.OK;
				response.message = "Unidades compradas com sucesso";

			}

			return response;
		}

		private void closeServerThread() {
			try {
				System.out.println("closing thread for " + userId);
				socket.close();
				// sslSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
