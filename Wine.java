import java.io.IOException;

public class Wine {

	String name;
	String image;
	int stock;
	int price;

/* 	public Wine(String name, String image) throws IOException {
		if (wineExists(name)) {
			return;
		}
		this.name = name;
		this.image = image;
		stock = price = 0;
	} */

	public String toString() {
		return String.format("%s | %s | %d", name, image);
	}

	// THIS BELONGS ON LISTINGS after making it work like it should
	// public static boolean boughtWasWine(String seller, String wine, int quantity)
	// throws IOException {
	// String wineInfo = Data.readWineInfoFromFile(wine);
	// System.out.println(wineInfo);
	// String userInfo = Data.readUserInfoFromFile(seller);
	// System.out.println(userInfo);
	// String[] userTokens = userInfo.split(":");
	// String[] wineTokens = wineInfo.split(":");
	// wineTokens[QUANTITY] = String.valueOf(Integer.parseInt(wineTokens[QUANTITY])
	// - quantity);
	// userTokens[BALANCE] = String.valueOf(Integer.parseInt(userTokens[BALANCE]) -
	// Integer.parseInt(wineTokens[VALUE]) * quantity);
	// String newWineLine = String.join(":", wineTokens);
	// String newUserLine = String.join(":", userTokens);
	// return Data.updateLineWines(wineInfo, newWineLine) &&
	// Data.updateLineUsers(userInfo, newUserLine);
	// }

	/**
	 * Adds the classification to the classification list and the new avg value calculated
	 * returns true if the classification was added
	 * @param wine
	 * @param classification
	 * @return
	 * @throws IOException
	 */
	public static boolean classify(String wine, int classification) throws IOException {
		String info = Data.readWineInfoFromFile(wine);
		String[] infoTokens = info.split(":");
		StringBuilder newLine = new StringBuilder();
		if(infoTokens.length == 2){
			newLine.append(infoTokens[0] + ":" + infoTokens[1] + ":" + String.valueOf(classification) + ":" + String.valueOf(classification));
			return Data.updateLineWines(info, newLine.toString());
		}

		//avg and write
		//TODO CHANGE TO ONLY CALCULATE AVG ON VIEW AND NOT ON CLASSIFY
		int sumClassifications = Wine.sumClassifications(infoTokens[2]) + classification;
		double avgClassification = (double)sumClassifications / (infoTokens[2].split(",").length + 1);

		//String newLine = String.join(":", infoTokens);
		newLine.append(infoTokens[0] + ":" + infoTokens[1] + ":" + infoTokens[2] + "," + String.valueOf(classification) + ":" + String.valueOf(avgClassification));
		return Data.updateLineWines(info, newLine.toString());
	}

	/**
	 * Adds a wine to the wine file with the user that added it
	 * Example: wine:username
	 * returns true if the wine was added
	 * @param wine
	 * @param user
	 * @return
	 * @throws IOException
	 */
 	public static boolean addWine(String wine, String user) throws IOException {
		String wineInfo = Data.readWineInfoFromFile(wine);
		if (wineInfo != null){
			return false;
		}
		return Data.writeOnFile(wine + ":" + user + ":" + "" + ":" + "", Constants.WINE_FILE);
	}

	public static boolean wineExists(String wine) throws IOException {
		return Data.readWineInfoFromFile(wine) != null;
	}

	/**
	 * Returns the name of the wine
	 * @param wine
	 * @return
	 * @throws IOException
	 */
	public static String getWine(String wine) throws IOException {
		String wineInfo = Data.readWineInfoFromFile(wine);
		String[] wineTokens = null;
		if (wineInfo != null) {
			wineTokens = wineInfo.split(":");
		}
		return wineTokens[0];
	}

	/**
	 * Sums all the values from a String array seperated by "," and returns the sum (Integer)
	 * @param classifications
	 * @return
	 */
	public static int sumClassifications(String classifications) {
		int sum = 0;
		String[] classificationsTokens = classifications.split(",");
		for (int i = 0; i < classificationsTokens.length; i++) {
			sum += Integer.parseInt(classificationsTokens[i]);
		}
		return sum;
	}
}
