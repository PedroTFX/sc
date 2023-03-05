import java.util.Hashtable;

public class Wine {
	String seller;
	String name;
	String image;
	int toSell;
	int stock;
	int price;
	Hashtable<User, Integer> reviews;

	public Wine(String name, String image, String seller) {
		this.name = name;
		this.image = image;
		this.seller = seller;
		stock = price = 0;
		reviews = new Hashtable<User, Integer>();
	}

	public String toString() {
		return String.format("%s | %s | %d | %d | %s", name, image, stock, price, reviews);
	}
}
