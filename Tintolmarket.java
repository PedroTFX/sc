import java.net.*;
import java.io.*;

public class Tintolmarket {

    
    private static int port = 12345;

    public static void main(String[] args) throws IOException {
        port = (args[0] != port) ? args[0]: port ;

        ServerSocket socket = new ServerSocket(port);
        Socket s = socket.accept();

        



        socket.close();
    }
}
