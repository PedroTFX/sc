import java.util.HashMap;
import java.util.Hashtable;

public class User {
	String id;
	String password;
	Hashtable<String, Wine> wineList;
	int balance;
	Hashtable<String, String[]> messages;

	User(String id, String password) {
		this.id = id;
		this.password = password;
		wineList = new Hashtable<String, Wine>();
		balance = 200;
		messages = new Hashtable<String, String[]>();
	}

	public String toString() {
		return String.format("%s | %s | %s | %d | %s", id, password, wineList, balance, messages);
	}
}
