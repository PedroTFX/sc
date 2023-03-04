import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.util.Hashtable;

class UserInfo{

    private static Hashtable<String, String> userTable;

    /**
     * Constructor that reads the file and puts in usertable
     */
    public UserInfo(String filename){
        userTable = new Hashtable<String, String>();
        try{
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String line;
            while((line = in.readLine()) != null){
                String[] split = line.split(":");
                userTable.put(split[0], split[1]);
            }
            in.close();
        }catch(IOException e){
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    /**
     * Returns if the user and the password are a match
     * Assumes that the user is registered
     */
    public static boolean isAuthenticated(String user, String password){
        return userTable.get(user).equals(password);
    }

    /**
     * Returns if the user is registered
     */
    public static boolean isRegistered(String user){
        return userTable.containsKey(user);
    }

    /**
     * Registers a user on the hashtable
     * 
     * TODO: write to file ???
     * If the user already exists do we authenticate or not?
     * 
     * @param user the username
     * @param password the password
     * @return true if the user was registered, false if the user already exists
     */
    public static boolean registerUser(String filename, String user, String password){
        if(isRegistered(user)){
            System.err.println("User already exists");
            return false;
        }
        
        //write to file
        userTable.put(user, password);
        return writeToFile(filename, user + ":" + password);
    }

    private static boolean writeToFile(String filename, String line){
        try{
            FileWriter out = new FileWriter(filename, true);
            out.write(line);
            out.close();
            return true;
        }catch(IOException e){
            System.err.println(e.getMessage());
            System.exit(-1);
            return false;
        }
    }

    /**
     * Tests
     */
    // public static void main(String[] args) {
    //     //tests
    //     UserInfo ui = new UserInfo("users.txt");
    //     System.out.println(ui.isAuthenticated("user1", "pass1")); //true
    //     System.out.println(ui.isAuthenticated("user1", "pass2")); //false
    //     System.out.println(ui.isAuthenticated("user2", "pass2")); //true

    //     System.out.println(ui.isRegistered("user1"));             //true
    //     System.out.println(ui.isRegistered("user2"));             //true
    //     System.out.println(ui.isRegistered("user3"));             //false

    //     registerUser("user3", "pass3");
    //     System.out.println(ui.isRegistered("user3"));             //true
    //     System.out.println(ui.isAuthenticated("user3", "pass3")); //true
    // }
}