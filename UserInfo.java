import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Hashtable;

class UserInfo{

    private static Hashtable<String, String> userTable;
    private static Hashtable<String, String> wineTable;
    private static String serverPath = "users.txt";

    /**
     * Constructor that reads the file and puts in usertable
     */
    public UserInfo(String fileString){
        userTable = new Hashtable<String, String>();
        wineTable = new Hashtable<String, String>();
        try{
            BufferedReader in = new BufferedReader(new FileReader(serverPath));
            String line;

            //read user table
            while((line = in.readLine()) != null){
                if(line.equals("")){
                    break;
                }
                String[] split = line.split(":");
                userTable.put(split[0], split[1]);
            }

            //read wine table
            while((line = in.readLine()) != null){
                if(line.equals("")){
                    break;
                }
                String[] split = line.split(":");
                wineTable.put(split[0], split[1]);
            }
            in.close();
        }catch(IOException e){
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    /**
     * Returns if the user and the password are a match
     */
    public static boolean authentication(String user, String password){
        if(password.equals(null)){
            return false;
        }

        //if the user is not registered, register
        if(!isRegistered(user)){
            return registerUser(user, password);
        }

        //if the user is registered, check if the password is correct
        return userTable.get(user).equals(password);
    }

    /**
     * Returns if the user is registered
     */
    public static boolean isRegistered(String user){
        if(user.equals(null)){
            System.out.println("User is null");
            return false;
        }
        return userTable.containsKey(user);
    }

    /**
     * Registers a user on the hashtable and writes to file
     * @param user the username
     * @param password the password
     * @return true if the user was registered, false if the user already exists
     */
    public static boolean registerUser(String user, String password){
        if(isRegistered(user)){      //can happen in registration NOT IN AUTHENTICATION
            System.err.println("User already exists");
            return false;
        }

        //write to file
        userTable.put(user, password);
        return writeToFile(user + ":" + password);
    }

    private static boolean writeToFile(String line){
        try{
            FileWriter out = new FileWriter(serverPath, true);
            out.write(line + "\n");
            out.close();
            return true;
        }catch(IOException e){
            System.err.println(e.getMessage());
            System.exit(-1);
            return false;
        }
    }

    private String getPassword(String user){
        return userTable.get(user.split(":")[0].split(";")[0]);
    }

/*     private String getBalance(String user){
        return user.split(";")[1];
    } */

    private String getWines(String user){
        return user.split(";")[2];
    }

/*     private boolean UserChange(String user,String typeOfChange, String change){
        //init
        String wines = getWines(user);
        String balance = getBalance(user);

        //change hashtable
        if(typeOfChange.equals("wine")){
            wines = addWine(user, change);
        }else if(typeOfChange.equals("balance")){
            balance = addBalance(user, change);
        }
        String newUserInfo = getPassword(user) + ";" + balance + ";" + wines;
        userTable.put(user, newUserInfo);

        //write to file
        for(String s : userTable.keySet()){
            writeToFile(s + ":" + userTable.get(s));
        }
        for(String s : wineTable.keySet()){
            writeToFile(s + ":" + wineTable.get(s));
        }

        return true;
    } */



/*     private String addBalance(String user, String amount) {
        int balance = Integer.parseInt(getBalance(user));
        balance += Integer.parseInt(amount);
        return Integer.toString(balance);
    } */

    private String addWine(String user, String wine/*, String image*/) {
        String[] wines = getWines(user).split(",");
        String newWines = "";
        for(String s : wines){
            //if the wine is negative (-VinhaBranco), remove it
            if(wine.charAt(0) == '-' && s.equals(wine.substring(1))){
                continue;
            }
            newWines += s + ",";
        }
        newWines += wine;

        // try{
        //     FileWriter out = new FileWriter(serverPath);
        //     BufferedReader in = new BufferedReader(new FileReader(serverPath));
        //     String line;
        //     while((line = in.readLine()) != null){
        //         if(line.split(":")[0].equals(wine) && wine.charAt(0) != '-'){
        //             out.write(wine + ":" + image + ":::");
        //             out.close();
        //             return newWines;
        //         }
        //     }
        // }catch(IOException e){
        //     System.err.println(e.getMessage());
        //     System.exit(-1);
        // }
        return newWines;
    }

/*     public static boolean addWine(String wine_name, String image){
        wineTable.put(wine_name, image);
        return writeToFile(wine_name + ":" + image);
    }

    public static boolean addBalance(String user, String amount){
        UserInfo ui = new UserInfo("hi");
        return ui.UserChange(user, "balance", amount);
    }

    public static boolean sellWine(String user1, String wine, String user2){
        //TODO: check if user1 has wine
        //TODO: check if user2 has enough money
        //TODO: check if wine exists
        //TODO: check if amount is not null
        //TODO: check if user 2 can buy wine
    }

    public static boolean classifyWine(String user, String wine, String classification){
        //TODO: check if user has wine
        //TODO: check if wine exists
        //TDOO: check if classification is not null

    } */

/*     public static getWine(String user, wine){
        //TODO: return wine
    } */

    /**
     * Tests
     */
    public static void main(String[] args) {
       /*  //tests
        UserInfo ui = new UserInfo("hi");
        for(String s : userTable.keySet()){
            System.out.println(s + " " + userTable.get(s));
        }
        for(String s : wineTable.keySet()){
            System.out.println(s + " " + wineTable.get(s));
        }
        //get pass
        System.out.println(ui.getPassword("user1:pass1;100;1,2,3"));    //pass1
        //get balance
        System.out.println(ui.getBalance("user1:pass;100;1,2,3"));    //100
        // get wines
        for(String s : ui.getWines("user1:pass;100;1,2,3").split(",")){
            System.out.println(s);
        }
        //add wine
        System.out.println(ui.addWine("user1:pass;100;1,2,3", "4"));  //1,2,3,4
        //add balance
        System.out.println(ui.addBalance("user1:pass;100;1,2,3", "50"));  //150
        //change user
        System.out.println(ui.UserChange("user1:pass;100;1,2,3", "wine", "4"));  //true
        System.out.println(ui.UserChange("user1:pass;100;1,2,3", "balance", "50"));  //true

        for(String s : userTable.keySet()){
            System.out.println(s + " " + userTable.get(s));
        } */

        // System.out.println(ui.authentication("user1", "pass1"));  //true
        // System.out.println(ui.authentication("user1", "pass2"));  //false
        // System.out.println(ui.authentication("user2", "pass2"));  //true

        // System.out.println(ui.isRegistered("user1"));             //true
        // System.out.println(ui.isRegistered("user2"));             //true
        // System.out.println(ui.isRegistered("user3"));             //false

        // System.out.println(registerUser("user3", "pass3"));       //true
        // System.out.println(ui.isRegistered("user3"));             //true
        // System.out.println(ui.authentication("user3", "pass3"));  //true
    }

	public static void addWineToUser(String user, String wine/* , Image imagem */){
		String[] infoUser = Data.readInfoFromFile(user, "users.txt").split(";");
		System.out.println(infoUser[2]);
		infoUser[2] = infoUser[2] + "," + wine;
		System.out.println(infoUser[2]);

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter("users.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			System.out.println(infoUser.toString());
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < infoUser.length; i++) {
				sb.append(infoUser[i] + ";");
			}
			bw.write(user + ":" + sb.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static int getBalance(String user){
		String[] userInfo = Data.readInfoFromFile(user, "users.txt").split(";");
		return Integer.parseInt(userInfo[1]);
	}


}
