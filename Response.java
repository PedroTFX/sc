import java.io.Serializable;
import java.util.Hashtable;

public class Response implements Serializable {
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
	Hashtable<String, String[]> messages;

	public String toString() {
		return String.format("%s | %s | %s | %d | %s | %d | %d | %d | %s", type, message, image,
				averageWineClassification, seller, price, quantity, balance, messages);
	}

	private Response(Type operation) {
		this.type = operation;
	}

	public Response() {

	}

	static public Response createAuthenticateResponse(String message, boolean ok) {
		Response response = new Response(ok ? Type.OK : Type.ERROR);
		response.message = message;
		return response;
	}
}// class
