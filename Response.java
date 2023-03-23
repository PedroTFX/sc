import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

public class Response implements Serializable {
	enum Type {
		OK, ERROR, VIEW, READ
	}

	Type type;
	String wine;
	String message;
	String image;
	double averageWineClassification;
	String seller;
	int price;
	int quantity;
	int balance;
	Hashtable<String, ArrayList<String>> messages;

	public void responseToString() {
		System.out.println("RESPONSE:");
		System.out.println("Type: " + type.toString());
		System.out.println("Wine: " + wine);
		System.out.println("Error message: " + message);
		System.out.println("Image name: " + image);
		System.out.println("avgClassification: " + averageWineClassification);
		System.out.println("Seller: " + seller);
		System.out.println("Quantity: " + quantity);
		System.out.println("Balance: " + balance);
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
