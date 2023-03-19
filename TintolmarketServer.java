import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

public class TintolmarketServer implements Serializable {
	private static int port = 12345;

	public static void main(String[] args) {
		System.out.println("servidor: main");
		TintolmarketServer server = new TintolmarketServer();
		// getting port from args else defaults to 1234
		port = args.length > 0 ? Integer.parseInt(args[0]) : port;
		server.startServer(port);
	}

	/**
	 * Starts the server on the specified port
	 *
	 * @param port
	 */
	public void startServer(int port) {
		ServerSocket sSoc = null;

		try {
			sSoc = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}

		while (true) {
			try {
				Socket inSoc = sSoc.accept();
				new ServerThread(inSoc);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// sSoc.close();
	}

	// Threads utilizadas para comunicacao com os clientes
	class ServerThread extends Thread {
		private Socket socket = null;
		ObjectOutputStream outStream;
		ObjectInputStream inStream;
		String userId = null;

		ServerThread(Socket inSoc) {
			socket = inSoc;
			start();
		}

		public void run() {
			// Initialization
			init();
			System.out.println("Thread inicializada");

			// Authentication
			authenticate();
			System.out.printf("Utilizador %s autenticado\n", userId);

			// Listen to requests
			listen();

			// Close socket and thread
			close();
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
				Response response = new Response(); // Prepare response

				// Check if request is an authentication request
				if (request.operation != Request.Operation.AUTHENTICATE) {
					response.type = Response.Type.ERROR;
					response.message = "O utilizador tem que se autenticar primeiro!";
					outStream.writeObject(response);
					close();
				}

				userId = request.user;
				response.type = Response.Type.OK;
				response.message = "OK";
				// Check if user exists and password is correct
				if (!Data.confirmPassword(userId, request.password)) {
					response.type = Response.Type.ERROR;
					response.message = "Combinação userID/password incorreta";
					outStream.writeObject(response);
					close();
				}

				// Send response
				outStream.writeObject(response);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		private void listen() {
			System.out.printf("Listening to client %s requests...\n", userId);
			while (!socket.isClosed()) {
				try {
					Request request = (Request) inStream.readObject();
					Response response = processRequest(request);
					outStream.writeObject(response);

					if (response.type == Response.Type.VIEW) {
						// Ir à DB ler o nome da imagem deste vinho no disco
						String wineImageName = "portao.jpg"; // getWineImageName();
						// Ler imagem do disco
						BufferedImage image = WineImage.readImageFromDisk(WineImage.getImagePath("portao.jpg"));
						// Enviar imagem pela rede
						WineImage.sendImage(image, outStream);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}

		private Response processRequest(Request request) throws Exception {
			System.out.printf("REQUEST: %s\n", request);
			Response response = new Response();
			if (request.operation == Request.Operation.ADD) {
				// Read image from network
				BufferedImage image = WineImage.readImageFromNetwork(inStream);

				// Check if wine already exists
				boolean alreadyExists = Logic.addWine(userId, request.wine);
				if (alreadyExists) {
					response.type = Response.Type.ERROR;
					response.message = "Esse vinho já existe!";
					return response;
				}

				// Everything OK, save image to disk
				File folder = WineImage.createFolder();
				String[] imageTokens = request.image.split("\\.");
				String extension = imageTokens[imageTokens.length - 1];
				String wineImageName = WineImage.writeImageToFile(folder, image, extension);

				// Save wine name and image name to Database

				// Create response
				response.type = Response.Type.OK;

			} /*
				 * else if (request.operation == Request.Operation.SELL) {
				 * boolean exists = Logic.sellWine(userId, request.wine, request.quantity);
				 * if (!exists) {
				 * response.type = Response.Type.ERROR;
				 * response.message = "Esse vinho não existe!";
				 * } else {
				 * response.type = Response.Type.OK;
				 * }
				 * }
				 */ else if (request.operation == Request.Operation.VIEW) {
				String exists = Logic.viewWine(request.wine);
				exists = "porto";
				// nao apagar!!!!!!!!!!!!
				/*
				 * if (exists == null) {
				 * response.type = Response.Type.ERROR;
				 * response.message = "Este vinho nao existe";
				 * }
				 */
				// nao apagar!!!!!!!!!!!!!
				// response.seller = Logic.getSeller(request.wine);
				// response.averageWineClassification =
				// Logic.averageWineClasification(request.wine);
				response.type = Response.Type.OK;
				response.wine = request.wine;
			} /*
				 * else if (request.operation == Request.Operation.WALLET) {
				 * response.type = Response.Type.OK;
				 * response.balance = Integer.parseInt(Logic.wallet());
				 * } else if (request.operation == Request.Operation.CLASSIFY) {
				 * boolean reviewd = false;
				 * try {
				 * reviewd = Logic.classify(userId, request.stars);
				 * } catch (IOException e) {
				 * e.printStackTrace();
				 * }
				 * if (reviewd) {
				 * response.type = Response.Type.OK;
				 * }
				 * } else if (request.operation == Request.Operation.TALK) {
				 * boolean messageSent = Logic.sendMessage(userId, request.user,
				 * request.message);
				 * if (messageSent) {
				 * response.type = Response.Type.OK;
				 * } else {
				 * response.type = Response.Type.ERROR;
				 * response.message = "Recetor não existe";
				 * }
				 * } else if (request.operation == Request.Operation.READ) {
				 * Hashtable<String, String[]> messages = Logic.getMessage(userId);
				 * if (messages != null) {
				 * response.messages = messages;
				 * response.type = Response.Type.OK;
				 * } else {
				 * response.type = Response.Type.ERROR;
				 * }
				 * }
				 */

			System.out.printf("RESPONSE: %s\n", response);
			return response;
		}

		private void close() {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(-1);
		}
	}

}
