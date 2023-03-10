import java.io.IOException;
import java.util.Hashtable;

public class Logic {

    private static String filename = "users.txt";
    private static User currentUser = null;

    public User autheticate(String user , String password) throws IOException{
        return (currentUser = new User(user, password));
    }

    /**
     * Adds a wine to the user's wine list
     * Returns true if it was possible to add the wine
     * user1:pass1:0:vinho1,vinho2
     * @param user
     * @param wine
     * @return
     */
    public static boolean addWine(String user, String wine){
        //read user info from file and init
        String allInfo = currentUser.getWines();
        String[] userInfo = allInfo.split(":");
        String password = userInfo[1];
        int balance = Integer.parseInt(userInfo[2]);
        String vinhos = userInfo[3] == null ? "" : userInfo[3];

        //update user wines info
        boolean hasWine = false;
        for (String vinho : vinhos) {
            if(vinho.equals(wine)){
                hasWine = true;
                break;
            }
        }

        vinhos = vinhos.equals("") && !hasWine ? wine : vinhos + "," + wine;
        //TODO ADD TO THE WINE TXT FILE

        //write to file
        return Data.updateLine(allInfo, user + ":" + password + ":" + balance + ":" + vinhos, filename);
    }

    //TODO the wine list must have a way to find the wine by seller
    public static boolean buyWine(String seller, String wine, int quantity) throws IOException{
        //check if wine is available
        String[] wineInfo = Data.readWineInfoFromFile(wine + ";" + seller).split(":");
        int wineAvailability = Integer.parseInt(wineInfo[1]);
        if(wineAvailability < quantity){
            return false;
        }

        //check if user has enough money
        int winePrice = Integer.parseInt(wineInfo[3]);
        int wineTotalPrice = winePrice * quantity;
        if(currentUser.getBalance() < wineTotalPrice){
            return false;
        }

        //Atomic transaction
        //update wine seller balance
        String safeGuard = currentUser.toString();
        if(currentUser.updateUser(wine, seller, wineTotalPrice, wine)){
            
        }
        

        //update wine availability and USERS balance
        return currentUser.buyWine(wineTotalPrice) && Wine.boughtWasWine(seller, wine, quantity);
    }

    public static String wallet(){
        return String.valueOf(currentUser.getBalance());
    }

    public static boolean classify(String wine, int classification) throws IOException{
        if(classification < 0 && classification > 5){
            return false;
        }
        return Wine.classify(wine, classification);
    }

    public static boolean sendMessage(String dest, String message){
        //TODO seperate list for user search ???
        return currentUser.sendMessage(dest, message);
    }

    public static String[] read(){
        return currentUser.getMSM();
    }

    public static void main(String[] args) {

    }

	public static boolean sellWine(String userId, String wine, int quantity) {
		return false;
	}

	public static boolean viewWine(String wine) {
		return false;
	}

	public static String getSeller(String wine) {
		return null;
	}

	public static int averageWineClasification(String wine) {
		return 0;
	}

	public static boolean sendMessage(String userId, String user, String message) {
		return false;
	}

	public static Hashtable<String, String[]> getMessage(String userId) {
		return null;
	}
}
