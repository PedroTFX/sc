import java.util.Dictionary;
import java.util.HashMap;

public class Response {
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
}
