import java.io.IOException;
import java.util.zip.Inflater;

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

	public static boolean classify(String wine, int classification) throws IOException {
		String info = Data.readWineInfoFromFile(wine);
		String[] infoTokens = info.split(":");
		StringBuilder newLine = new StringBuilder();
		if(infoTokens.length == 2){
			newLine.append(infoTokens[0] + ":" + infoTokens[1] + ":" + String.valueOf(classification) + ":" + String.valueOf(classification));
			return Data.updateLineWines(info, newLine.toString());
		}

		int sumClassifications = Logic.sumClassifications(infoTokens[2]) + classification;
		double avgClassification = (double)sumClassifications / (infoTokens[2].split(",").length + 1);
		//String newLine = String.join(":", infoTokens);
		newLine.append(infoTokens[0] + ":" + infoTokens[1] + ":" + infoTokens[2] + "," + String.valueOf(classification) + ":" + String.valueOf(avgClassification));
		return Data.updateLineWines(info, newLine.toString());
	}

 	public static boolean addWine(String wine, String user) throws IOException {
		String wineInfo = Data.readWineInfoFromFile(wine);
		if (wineInfo != null){
			return false;
		}
		return Data.writeOnFile(wine + ":" + user + ":" + "" + ":" + "", Data.WINE_FILE);
	}

	public static boolean wineExists(String wine) throws IOException {
		return Data.readWineInfoFromFile(wine) != null;
	}

	public static String getWine(String wine) throws IOException {
		String wineInfo = Data.readWineInfoFromFile(wine);
		String[] wineTokens = null;
		if (wineInfo != null) {
			wineTokens = wineInfo.split(":");
		}
		return wineTokens[0];
	}
}
