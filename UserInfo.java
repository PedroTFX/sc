import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.util.Hashtable;

class UserInfo{

    private static Hashtable<String, String> userTable;
    private static String serverPath = "users.txt";

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
        return writeToFile(serverPath, user + ":" + password + "\n\n");
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
    public static void main(String[] args) {
        //tests
        UserInfo ui = new UserInfo("users.txt");
        System.out.println(ui.authentication("user1", "pass1"));  //true
        System.out.println(ui.authentication("user1", "pass2"));  //false
        System.out.println(ui.authentication("user2", "pass2"));  //true

        System.out.println(ui.isRegistered("user1"));             //true
        System.out.println(ui.isRegistered("user2"));             //true
        System.out.println(ui.isRegistered("user3"));             //false

        System.out.println(registerUser("user3", "pass3"));       //true
        System.out.println(ui.isRegistered("user3"));             //true
        System.out.println(ui.authentication("user3", "pass3"));  //true
    }
}