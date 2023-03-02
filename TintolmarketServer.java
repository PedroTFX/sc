import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Dictionary;
import java.util.Hashtable;

public class TintolmarketServer {

	private static int port = 12345;
	private static Hashtable<String, String> userTable;
    
	public static void main(String[] args) {
		System.out.println("servidor: main");
		TintolmarketServer server = new TintolmarketServer();
		userTable = new Hashtable<String, String>();

		//getting port from args else defaults to 12345
		port = args.length > 0 ? Integer.parseInt(args[0]) : port;
		server.startServer(port);
	}

	/**
	 * Starts the server on the specified port
	 * @param port
	 */
	public void startServer (int port){
		ServerSocket sSoc = null;
        
		try {
			sSoc = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
         
		while(true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
		    }
		    catch (IOException e) {
		        e.printStackTrace();
		    }
		    
		}
		//sSoc.close();
	}


	//Threads utilizadas para comunicacao com os clientes
	class ServerThread extends Thread {

		private Socket socket = null;

		ServerThread(Socket inSoc) {
			socket = inSoc;
			System.out.println("thread do server para cada cliente");
		}
 
		public void run(){
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

				String user = null;
				String pass = null;
			
				try {
					user = (String)inStream.readObject();
					pass = (String)inStream.readObject();
					
				}catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}


				//TODO: refazer
				//este codigo apenas exemplifica a comunicacao entre o cliente e o servidor
				//nao faz qualquer tipo de autenticacao

				//checks if the user exists in the userTable, if it does, checks if the password is correct
				//returns OK if the user exists and the password is correct, ERROR otherwise
				outStream.writeObject((checkUser(user, pass))?Response.Type.OK:Response.Type.ERROR);
				
				//TODO: loop between client and server for the actual communication

				outStream.close();
				inStream.close();
 			
				socket.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/**
		 * Checks if the user exists in the userTable, if it does, checks if the password is correct
		 * @param user
		 * @param pass
		 * @return
		 */
		private boolean checkUser(String user, String pass) {
			if(userTable.contains(user)) {
				return userTable.get(user).equals(pass);
			}
			else {
				userTable.put(user, pass);
				return true;
			}
		}
	}
}
