import java.io.Serializable;

public class WineSale implements Serializable {
	String uuid;
	String name;
	int price;
	int quantity;

	WineSale(String uuid, String name, int price, int quantity) {
		this.uuid = uuid;
		this.name = name;
		this.price = price;
		this.quantity = quantity;
	}
}
