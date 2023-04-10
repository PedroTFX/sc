public class User {
	/*
     * Returns if the user and the password are a match
     * If the user is not registered, register
     */
    public boolean authenticate(String user, String password) throws /* IO */Exception{
		if(user == null || password == null){
			return false;
		}
        if(Data.readUserInfoFromFile(user) != null){
            return Data.confirmPassword(user, password);
        }
        return Data.registerUser(user + ":" + password + ":" + 200);
    }
}
