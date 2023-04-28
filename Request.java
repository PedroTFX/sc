import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;

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
		WineSale wineSale;

		ListWine(WineSale wineSale) {
			this.wineSale = wineSale;
		}
	}

	static class ViewWine implements Serializable {
		String name;

		ViewWine(String name) {
			this.name = name;
		}
	}

	static class BuyWine implements Serializable {
		WinePurchase winePurchase;
/* 		String uuid;
		String name;
		String seller;
		int quantity;
		ArrayList<String> signedNfts;
		ArrayList<String> transaction; */

		BuyWine(WinePurchase winePurchase/* String uuid, String name, String seller, int quantity, ArrayList<String> signedNfts, ArrayList<String> transaction */) {
			this.winePurchase = winePurchase;
/* 			this.uuid = uuid;
			this.name = name;
			this.seller = seller;
			this.quantity = quantity;
			this.signedNfts = signedNfts;
			this.transaction = transaction; */
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
		byte[] encryptedKey;
		Talk(String user, String message, byte[] encryptedKey) {
			this.user = user;
			this.message = message;
			this.encryptedKey = encryptedKey;
		}
	}

	static class Wallet implements Serializable{
		String user;
		Wallet(String user) {

		}
	}

	static class Signed<T extends Serializable> implements Serializable {
		T transaction;
		String base64Signature;

		public Signed(T transaction, PrivateKey key) throws Exception {
			this.transaction = transaction;
			this.base64Signature = sign(transaction, key);
		}

		public boolean verify(PublicKey key) throws Exception {
			byte[] signature = Base64.getDecoder().decode(base64Signature);
			byte[] toVerify = getTransactionBytes();
			return SecurityRSA.verifySignature(toVerify, signature, key);
		}

		private String sign(T transaction, PrivateKey key) throws Exception {
			byte[] signedBytes = SecurityRSA.sign(getTransactionBytes(), key);
			return Base64.getEncoder().encodeToString(signedBytes);
		}

		private byte[] getTransactionBytes() throws Exception {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(transaction);
			oos.close();
			return baos.toByteArray();
		}
	}
}
