import java.io.Serializable;

public class Listing implements Serializable {
	String seller;
	String name;
	int price;
	int quantity;

	Listing(String seller, String name, int price, int quantity) {
		this.seller = seller;
		this.name = name;
		this.price = price;
		this.quantity = quantity;
	}
}
