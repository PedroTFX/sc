import java.io.IOException;

public class Listings {
    /**
     * Adds a listing to the wineListings
     * Checks if the wine already exists in the listing given by the same user and if it does it sums the available quantities
     * @param userId
     * @param wine
     * @param quantity
     * @return
     * @throws IOException
     */
    public static boolean addListing(String userId, String wine, int quantity, int value) throws /* IO */Exception {
		String sell = Data.readSellInfo(wine,userId);
		if (sell != null) {
			String[] sellTokens = sell.split(":");
			sellTokens[2] = String.valueOf(quantity + Integer.parseInt(sellTokens[2]));
			sellTokens[3] = String.valueOf(value);
			return Data.updateImageSellsFile(sell, String.join(":", sellTokens));
		}

		return Data.writeOnFile(wine + ":" + userId + ":" + String.valueOf(quantity) + ":" + String.valueOf(value), Constants.SELLS_FILE);

    }

}
