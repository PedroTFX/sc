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
        if(seller == null || wine == null || quantity < 0){
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
		//System.out.println("entramos!!!!!!!!!!!!!!!!!!!!!");
		String sell = Data.readSellInfo(wine);
		//System.out.println("SELL: "+ sell);
		if (sell != null) {
			String[] sellTokens = sell.split(":");
			sellTokens[2] = String.valueOf(quantity + Integer.parseInt(sellTokens[2]));
			return Data.updateImageSellsFile(sell, String.join(":", sellTokens));
		}

		return Data.writeOnFile(wine + ":" + userId + ":" + String.valueOf(quantity) + ":" + String.valueOf(value), Constants.SELLS_FILE);

        /* if(wineListings.containsKey(listingID)){
            value = Integer.parseInt(wineListings.get(listingID)) + quantity;
        }
        wineListings.put(listingID, Integer.toString(value));
        return wineListings.containsKey(listingID) && wineListings.get(listingID).equals(Integer.toString(value)); */
    }

    public static boolean buyListing(String seller, String wine, int quantity) {
        if(quantity < 0 || !available(seller, wine, quantity)){
            return false;
        }
        String listingID = seller + ":" + wine;
        int value = Integer.parseInt(wineListings.get(listingID)) - quantity;
        if(value == 0){
            wineListings.remove(listingID);
        }else{
            wineListings.put(listingID, Integer.toString(value));
        }
        return Data.updateListings(wineListings);
    }

}
