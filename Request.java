import java.io.Serializable;

public class Request implements Serializable {
	public enum Operation {
		AUTHENTICATE,
		ADD,
		SELL,
		VIEW,
		BUY,
		WALLET,
		CLASSIFY,
		TALK,
		READ,
		QUIT
	}

	Operation operation;
	
	String wine;
	String image;
	int value;
	int quantity;
	String seller;
	int stars;

	
	String user;
	String password;
	String message;

	public String toString() {
		return String.format("%s | %s | %s | %d | %d | %s | %d | %s | %s | %s", operation, wine, image, value, quantity,
				seller, stars, user, password, message);
	}

	private Request(Operation operation) {
		this.operation = operation;
	}

	static public Request createAuthenticateOperation(String user, String password) {
		Request authenticate = new Request(Operation.AUTHENTICATE);
		authenticate.user = user;
		authenticate.password = password;
		return authenticate;
	}

	static public Request createAddOperation(String wine, String image) {
		Request add = new Request(Operation.ADD);
		add.wine = wine;
		add.image = image;
		return add;
	}

	static public Request createSellOperation(String wine, int value, int quantity) {
		Request sell = new Request(Operation.SELL);
		sell.wine = wine;
		sell.value = value;
		sell.quantity = quantity;
		return sell;
	}

	static public Request createViewOperation(String wine) {
		Request view = new Request(Operation.VIEW);
		view.wine = wine;
		return view;
	}

	static public Request createBuyOperation(String wine, String seller, int quantity) {
		Request buy = new Request(Operation.BUY);
		buy.wine = wine;
		buy.seller = seller;
		buy.quantity = quantity;
		return buy;
	}

	static public Request createWalletOperation() {
		Request wallet = new Request(Operation.WALLET);
		return wallet;
	}

	static public Request createClassifyOperation(String wine, int stars) {
		Request classify = new Request(Operation.CLASSIFY);
		classify.wine = wine;
		classify.stars = stars;
		return classify;
	}

	static public Request createTalkOperation(String user, String message) {
		Request talk = new Request(Operation.TALK);
		talk.user = user;
		talk.message = message;
		return talk;
	}

	static public Request createReadOperation() {
		return new Request(Operation.READ);
	}
}
