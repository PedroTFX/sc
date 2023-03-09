import java.io.IOException;

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

    public static boolean buyWine(String seller, String wine, int quantity){
        //check if wine is available
        Data.readWineInfoFromFile(wine);
        int wineAvailability = Integer.parseInt(Data.readWineInfoFromFile(wine).split(":")[2]);
        if(wineAvailability < quantity){
            return false;
        }

        //check if user has enough money
        int winePrice = Integer.parseInt(Data.readWineInfoFromFile(wine).split(":")[3]);
        int wineTotalPrice = winePrice * quantity;
        if(currentUser.getBalance() < wineTotalPrice){
            return false;
        }

        //update wine availability and USERS balance
        return Wine.sellWine(seller, wine, quantity) && User.buyWine(int wineTotalPrice);
    }

    public static String wallet(){
        return currentUser.getBalance();
    }

    public static boolean classify(String wine, int classification){
        return Wine.classify(wine, classification);
    }

    public static boolean sendMessage(String user, String message){
        return currentUser.sendMessage(user, message);
    }

    public static String[] read(){
        return currentUser.getMSM();
    }

    public static void main(String[] args) {
        //test register authentication and isRegistered
        if(!authenticate("user1", "pass1")){ //true
            System.out.println("authenticate(user1, pass1) failed");  //true
        }
        if(authenticate("user1", "pass2")){ //false
            System.out.println("authenticate(user1, pass2) failed");  //false
        }
        if(!authenticate("user2", "pass2")){ //true
            System.out.println("authenticate(user2, pass2) failed");  //true
        }

        //test addWine
        if(!addWine("user1", "vinho1")){ //true
            System.out.println("addWine(user1, vinho1) failed");  //true
        }
        if(!addWine("user1", "vinho2")){ //true
            System.out.println("addWine(user1, vinho2) failed");  //true
        }

    }
}
