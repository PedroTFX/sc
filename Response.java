import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

public class Response implements Serializable {
	enum Type {
		AUTHNONCE, OK, ERROR, VIEWWINE, READ
	}

	Type type;
	Object payload;

	static class AuthNonce implements Serializable{
		long nonce;
		boolean newUser;

		AuthNonce(long nonce, boolean newUser) {
			this.nonce = nonce;
			this.newUser = newUser;
		}
	}

	static class OK implements Serializable{
		String message;

		OK(String message) {
			this.message = message;
		}
	}

	static class Error implements Serializable {
		String message;

		Error(String message) {
			this.message = message;
		}
	}

	static class ViewWineAndListings extends ViewWine {
		ViewWineAndListings(Wine wine, ArrayList<Listing> listings) {
			super(wine, listings);
		}
	}

	Response(Type type, Object payload){
		this.type = type;
		this.payload = payload;
	}

	static class ReadMessages implements Serializable {
		Hashtable<String, ArrayList<String>> messages;

		ReadMessages(Hashtable<String, ArrayList<String>> messages) {
			this.messages = messages;
		}
	}
}
