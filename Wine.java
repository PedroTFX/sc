import java.util.Hashtable;

public class Wine {
	String name;
	String image;
	int stock;
	int price;
	Hashtable<User, Integer> reviews;

	public Wine(String name, String image, String seller) {
		this.name = name;
		this.image = image;
		stock = price = 0;
		reviews = new Hashtable<User, Integer>();
	}

	public String toString() {
		return String.format("%s | %s | %d | %d | %s", name, image, stock, price, reviews);
	}
}
