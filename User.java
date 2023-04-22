import java.security.PublicKey;

public class User {
	String name;
	PublicKey key;
	int balance;

	User(String name, PublicKey key) {
		this(name, key, Constants.STARTING_BALANCE);
	}

	User(String name, PublicKey key, int balance) {
		this.name = name;
		this.key = key;
		this.balance = balance;
	}
}
