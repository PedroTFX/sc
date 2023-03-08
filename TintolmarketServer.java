import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class TintolmarketServer implements Serializable {

	private static int port = 12345;
	private static Hashtable<String, User> userTable;
	private static ArrayList<Wine> wines;

	public static void main(String[] args) {
		System.out.println("servidor: main");
		TintolmarketServer server = new TintolmarketServer();
		userTable = new Hashtable<String, User>();
		wines = new ArrayList<Wine>();
		UserInfo userInfo = new UserInfo("users.txt");

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
				int i = 0;
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
				outStream = new ObjectOutputStream(socket.getOutputStream());
				inStream = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void authenticate() {
			try {
				//init
				Request request = (Request) inStream.readObject();			// Read request
				Response response = new Response();							// Prepare response

				// Check if request is an authentication request
				if (request.operation != Request.Operation.AUTHENTICATE) {
					response.type = Response.Type.ERROR;
					response.message = "O utilizador tem que se autenticar primeiro!";
					outStream.writeObject(response);
					close();

				}

				// Check if user exists and password is correct

				response.type = Response.Type.OK;
				response.message = "OK";
				if (!UserInfo.authentication(request.user, request.password)) {
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
					outStream.writeObject(processRequest(request));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				/*
				 * // TODO: refazer
				 * // este codigo apenas exemplifica a comunicacao entre o cliente e o servidor
				 * // nao faz qualquer tipo de autenticacao
				 *
				 * // checks if the user exists in the userTable, if it does, checks if the
				 * // password is correct
				 * // returns OK if the user exists and the password is correct, ERROR otherwise
				 * if (request.operation == Request.Operation.AUTHENTICATE) {
				 * if ((checkUser(request.user, request.password))) {
				 * response.type = Response.Type.OK;
				 * response.message = "hahha";
				 * } else {
				 * response.type = Response.Type.ERROR;
				 * response.message = "NOT OK";
				 * }
				 * outStream.writeObject(response);
				 * System.out.println("autentica");
				 * }
				 *
				 * /*
				 * try {
				 * request = (Request) inStream.readObject();
				 * // pass = (String) inStream.readObject();
				 *
				 * } catch (ClassNotFoundException e1) {
				 * e1.printStackTrace();
				 * }
				 */

				// TODO: loop between client and server for the actual communication
				/*
				 * if (request.operation == Request.Operation.WALLET) {
				 * response.type = Response.Type.OK;
				 * response.balance = 200;
				 * outStream.writeObject(response);
				 * System.out.println("responsi ao wallet");
				 * } else {
				 * response.type = Response.Type.ERROR;
				 * response.balance = 199;
				 * }
				 * System.out.println("fim while");
				 * request = null;
				 * }
				 */
				System.out.println(userTable);
			}
		}

		private Response processRequest(Request request) {
			System.out.printf("REQUEST: %s\n", request);
			Response response = new Response();
			User user = userTable.get(userId);
			Wine wine = null;
			if (request.wine != null) {
				wine = user.wineList.get(request.wine);
			}
			if (request.operation == Request.Operation.ADD) {
				//User user = userTable.get(userId);
				if (user.wineList.containsKey(request.wine)) {
					response.type = Response.Type.ERROR;
					response.message = "Esse vinho já existe!";
				} else {
					Wine newWine = new Wine(request.wine, request.image, userId);
					user.wineList.put(request.wine, newWine);
					wines.add(newWine);
					response.type = Response.Type.OK;
				}
			} else if (request.operation == Request.Operation.SELL) {
				//User user = userTable.get(userId);
				//Wine wine = user.wineList.get(request.wine);
				if (wine == null) {
					response.type = Response.Type.ERROR;
					response.message = "Esse vinho não existe!";
				} else {
					wine.stock -= request.quantity;
					wine.price = request.value;
					response.type = Response.Type.OK;
				}
			} else if (request.operation == Request.Operation.VIEW) {
				if (wine == null) {
					response.type = Response.Type.ERROR;
					response.message = "Este vinho nao existe";
				}
				response.averageWineClassification = averageWineClasification(wine.reviews);
				response.type = Response.Type.OK;
			} else if(request.operation == Request.Operation.WALLET){
				response.type = Response.Type.OK;
				response.balance = user.balance;
			}
			System.out.printf("RESPONSE: %s\n", response);
			return response;
		}

		private int averageWineClasification(Hashtable<User, Integer> reviews) {
			int sum = 0;
			Set<User> userSet = reviews.keySet();
			for (User user : userSet) {
				sum += reviews.get(user);
			}
			return sum / userSet.size();
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
