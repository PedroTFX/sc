import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

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
		//sSoc.close();
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
					outStream.writeObject(processRequest(request));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}

		private Response processRequest(Request request) {
			System.out.printf("REQUEST: %s\n", request);
			Response response = new Response();
			if (request.operation == Request.Operation.ADD) {
				boolean newWine = Logic.addWine(userId, request.wine);
				//User user = userTable.get(userId);
				if (!newWine){
					response.type = Response.Type.ERROR;
					response.message = "Esse vinho já existe!";
				} else {
					response.type = Response.Type.OK;
				}
			} else if (request.operation == Request.Operation.SELL) {
				boolean exists = Logic.sellWine(userId, request.wine, request.quantity);
				if (!exists) {
					response.type = Response.Type.ERROR;
					response.message = "Esse vinho não existe!";
				} else{
					response.type = Response.Type.OK;
				}
			} else if (request.operation == Request.Operation.VIEW) {
				boolean exists = Logic.viewWine(request.wine);
				if (!exists) {
					response.type = Response.Type.ERROR;
					response.message = "Este vinho nao existe";
				}
				response.seller = Logic.getSeller(request.wine);
				response.averageWineClassification = Logic.averageWineClasification(request.wine);
				response.type = Response.Type.OK;
			} else if(request.operation == Request.Operation.WALLET){
				response.type = Response.Type.OK;
				response.balance = Integer.parseInt(Logic.wallet());
			} else if(request.operation == Request.Operation.CLASSIFY){
				boolean reviewd = false;
				try {
					reviewd = Logic.classify(userId, request.stars);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(reviewd){
					response.type = Response.Type.OK;
				}
			} else if(request.operation == Request.Operation.TALK){
				boolean messageSent = Logic.sendMessage(userId, request.user, request.message);
				if (messageSent) {
					response.type = Response.Type.OK;
				} else {
					response.type = Response.Type.ERROR;
					response.message = "Recetor não existe";
				}
			} else if(request.operation == Request.Operation.READ){
				Hashtable<String, String[]> messages = Logic.getMessage(userId);
				if (messages != null) {
					response.messages = messages;
					response.type = Response.Type.OK;
				} else{
					response.type = Response.Type.ERROR;
				}
			}
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
