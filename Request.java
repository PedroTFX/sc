import java.io.Serializable;
import java.security.PublicKey;

public class Request implements Serializable {
	public enum Type {
		AUTHUSERID,
		AUTHREGISTER,
		AUTHLOGIN,
		ADDWINE,
		LISTWINE,
		VIEWWINE,
		BUYWINE,
		WALLET,
		CLASSIFY,
		TALK,
		READ,
		TRANSACTIONS,
		QUIT,
	}

	Type type;
	Object payload;

	Request(Type type, Object payload) {
		this.type = type;
		this.payload = payload;
	}

	public static class AuthUserID implements Serializable {
		String userID;

		AuthUserID(String userID) {
			this.userID = userID;
		}
	}

	static class AuthLogin implements Serializable {
		long nonce;
		byte[] signedNonce;

		AuthLogin(long nonce, byte[] signedNonce) {
			this.nonce = nonce;
			this.signedNonce = signedNonce;
		}
	}

	static class AuthRegister extends AuthLogin {
		PublicKey key;

		AuthRegister(long nonce, byte[] signedNonce, PublicKey key) {
			super(nonce, signedNonce);
			this.key = key;
		}
	}

	static class AddWine implements Serializable {
		Wine wine;

		AddWine(Wine wine) {
			this.wine = wine;
		}
	}

	static class ListWine implements Serializable {
		String name;
		int price;
		int quantity;

		ListWine(String name, int price, int quantity) {
			this.name = name;
			this.price = price;
			this.quantity = quantity;
		}
	}

	static class ViewWine implements Serializable {
		String name;

		ViewWine(String name) {
			this.name = name;
		}
	}

	static class BuyWine implements Serializable {
		String name;
		String seller;
		int quantity;

		BuyWine(String name, String seller, int quantity) {
			this.name = name;
			this.seller = seller;
			this.quantity = quantity;
		}
	}

	static class ClassifyWine implements Serializable{
		String name;
		int stars;

		ClassifyWine(String name, int stars) {
			this.name = name;
			this.stars = stars;
		}
	}

	static class Talk implements Serializable{
		String user;
		String message;

		Talk(String user, String message) {
			this.user = user;
			this.message = message;
		}
	}

	static class Wallet implements Serializable{
		String user;
		Wallet(String user) {

		}
	}
}
