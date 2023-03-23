import java.io.IOException;
import java.util.Hashtable;

public class Listings {

    public static Hashtable<String, String> wineListings;

    /**
     * initializes the wineListings
     * gets listings data from file with Data.getListings()
     */
    public Listings() {
        wineListings = Data.getListings();
    }

    /**
     * Check the availability of the quantity wine in the Listings
     * @param seller
     * @param wine
     * @param quantity
     * @return
     */
    public static boolean available(String seller, String wine, int quantity) {
        if(seller == null || wine == null || quantity <= 0){
            return false;
        }
        String listingID = seller + ":" + wine;
        return wineListings.containsKey(listingID) && Integer.parseInt(wineListings.get(listingID)) >= quantity;
    }

    public static int getPrice(String seller, String wine) {
        return Integer.parseInt(wineListings.get(seller + ":" + wine));
    }

    /**
     * Returns all available listings for a given wine
     * @param wine
     * @return
     */
    public static String getListing(String wine) {
        String listing = "";
        for(String key : wineListings.keySet()){
            if(key.split(":")[1].equals(wine)){
                listing += key + ": " + wineListings.get(key) + "\n";
            }
        }
        return listing;
    }

    /**
     * Adds a listing to the wineListings
     * Checks if the wine already exists in the listing given by the same user and if it does it sums the available quantities
     * @param userId
     * @param wine
     * @param quantity
     * @return
     * @throws IOException
     */
    public static boolean addListing(String userId, String wine, int quantity, int value) throws IOException {
        // Data.readSellInfo could return an Array of all the listing form a user of that wine
        // if it does we have to see if the value is the same, what if the value is not???
        //TODO UPDATE THE READSELLINFO FUCNTION IN THE DATA
        String sell = Data.readSellInfo(userId, wine);

        if(sell != null){
            // get value of wine and check if the available wines have the same value
            int valueOfWine = Integer.parseInt(sell.split(":")[3]);
            if (valueOfWine == value) {
                String[] sellTokens = sell.split(":");
                sellTokens[2] = String.valueOf(quantity + Integer.parseInt(sellTokens[2]));
                return Data.updateSellsFile(sell, String.join(":", sellTokens));
            }
        }
        // case sell is null or value isnt the same as the one in the file
		return Data.writeOnFile(wine + ":" + userId + ":" + String.valueOf(quantity) + ":" + String.valueOf(value), Constants.SELLS_FILE);
    }   

    public static boolean buyListing(String seller, String wine, int quantity) {
        if(!available(seller, wine, quantity)){
            return false;
        }
        String listingID = seller + ":" + wine;
        int available_quantity = Integer.parseInt(wineListings.get(listingID)) - quantity;
        if(available_quantity == 0){
            wineListings.remove(listingID);
        }else{
            wineListings.put(listingID, Integer.toString(available_quantity));
        }
        return Data.updateListings(wineListings);
    }

}
