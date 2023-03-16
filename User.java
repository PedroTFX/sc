import java.io.IOException;
import java.util.Hashtable;

public class User {

	//init
	private String id;
	private String password;
	private int balance;
	private String messages;
	private String wines;

	/**
	 * Creates a new user and authenticates
	 * @param id
	 * @param password
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	User(String id, String password) throws IOException{
		if(authenticate(id, password)){
			String[] info = Data.readUserInfoFromFile(id).split(":");
			this.id = info[0];
			this.password = info[1];
			balance = Integer.parseInt(info[2]);
			// wines = info[3].equals(" ") ? info[3].split(",") : null; //TODO: fix exception
			// messages = info[3];//format to be defined
		}else{
			System.err.println("Authentication failed");
		}
	}

	public String toString() {
		return String.format("%s | %s | %s", id, password, balance);
	}

	/*
     * Returns if the user and the password are a match
     * If the user is not registered, register
     */
    public static boolean authenticate(String user, String password) throws IOException{
        if(Data.readUserInfoFromFile(user) != null){
            return Data.confirmPassword(user, password);
        }
        return Data.registerUser(user + ":" + password + ":" + 0);
    }

	// TODO: UPDATE FUNCITONS FOR PROPER USE

    // private static String getWines(String user) throws IOException {
    //     return currentUser.getWines();
    // }



    // private static boolean hasWine(String user, String wine){
    //     String[] wines = getWines(user).split(",");
    //     if(wines.length < 0){
    //         return false;
    //     }

    //     for (String vinho : wines) {
    //         if(vinho.equals(wine)){
    //             return true;
    //         }
    //     }
    //     return false;
    // }

	public static void main(String[] args) {
		//test register authentication and isRegistered
		try {
			User user = new User("user1", "pass1");
			System.out.println(user);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

    public int getBalance() {
        return balance;
    }

	public boolean updateUser(String user, String password, int balance, String wines) throws IOException {
		return Data.updateLineUsers(this.id + ":" + this.password + ":" + this.balance + ":" + this.wines, user + ":" + password + ":" + balance + ":" + wines);
	}

	public static boolean buy(int quantity, int i) {
		return false;
	}

    public String getId() {
        return id;
    }
}
