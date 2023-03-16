
public class MsmContainer {

    public static boolean sendMessage(String from, String dest, String message) {
        if(dest == null || message == null || dest.length() <= 0 || message.length() < 0){
            return false;
        }
        return Data.sendMSM(dest + ":" + from + ":" + message);
    }

    public static String[] getMSM(User currentUser) {
        return Data.readMSM(currentUser.getId());
    }

}
