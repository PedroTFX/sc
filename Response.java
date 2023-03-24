import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

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
		if(this.wine != null){
			System.out.println("Wine: " + wine);
		}
		if (this.message != null) {
			System.out.println("Error message: " + message);
		}
		if (this.image != null) {
			System.out.println("Image name: " + image);
		}
		if (this.averageWineClassification > -1) {
			System.out.println("avgClassification: " + averageWineClassification);
		}
		if (this.seller != null) {
			System.out.println("Seller: " + seller);
		}
		if (this.quantity > -1) {
			System.out.println("Quantity: " + quantity);
		}
		if (this.balance > -1) {
			System.out.println("Balance: " + balance);
		}
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
