import java.io.Serializable;
import java.util.HashMap;

public class Response implements Serializable{
	enum Type {
		OK, ERROR
	}


	Type type;
	String message;
	String image;
	int averageWineClassification;
	String seller;
	int price;
	int quantity;
	int balance;
	HashMap<String, String[]> messages;

	private Response(Type operation) {
		this.type = operation;
	}

	public Response(){
		
	}

	static public Response createAuthenticateResponse(String message, boolean ok) {
		Response response = new Response(ok ? Type.OK :Type.ERROR);
		response.message = message;
		return response;
	}
}//class
