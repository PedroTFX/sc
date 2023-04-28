import java.io.Serializable;

public class WinePurchase implements Serializable {
	String uuid;
	String name;
	String seller;
	int quantity;

	WinePurchase(String uuid, String name, String seller, int quantity) {
		this.uuid = uuid;
		this.name = name;
		this.seller = seller;
		this.quantity = quantity;
	}
}
