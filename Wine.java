import java.io.IOException;

public class Wine {

	String name;
	String image;
	int stock;
	int price;

	public Wine(String name, String image) {
		if(wineExists(name)){
			return;
		}
		this.name = name;
		this.image = image;
		stock = price = 0;
		
	}

	public String toString() {
		return String.format("%s | %s | %d", name, image);
	}

	// public static boolean boughtWasWine(String seller, String wine, int quantity) throws IOException {
	// 	String wineInfo = Data.readWineInfoFromFile(wine);
	// 	System.out.println(wineInfo);
	// 	String userInfo = Data.readUserInfoFromFile(seller);
	// 	System.out.println(userInfo);
	// 	String[] userTokens = userInfo.split(":");
	// 	String[] wineTokens = wineInfo.split(":");
	// 	wineTokens[QUANTITY] = String.valueOf(Integer.parseInt(wineTokens[QUANTITY]) - quantity);
	// 	userTokens[BALANCE] = String.valueOf(Integer.parseInt(userTokens[BALANCE]) - Integer.parseInt(wineTokens[VALUE]) * quantity);
	// 	String newWineLine = String.join(":", wineTokens);
	// 	String newUserLine = String.join(":", userTokens);
	// 	return Data.updateLineWines(wineInfo, newWineLine) && Data.updateLineUsers(userInfo, newUserLine);
	// }

	public static boolean classify(String wine, int classification) throws IOException {
		String info = Data.readWineInfoFromFile(wine);

		String[] infoTokens = info.split(":");
		infoTokens[infoTokens.length - 1] = infoTokens[infoTokens.length - 1] + "," + String.valueOf(classification);

		String newLine = String.join(":", infoTokens);

		return Data.updateLineWines(info, newLine);
	}

	public static void main(String[] args) throws IOException {
/* 		boolean boughtWasWine = boughtWasWine("user", "porto", 1);
		if (boughtWasWine) {
			System.out.println("TRINTA!!!");
		} */

		boolean classify = classify("porto", 4);
		if (classify) {
			System.out.println("TRRRRRINTAAAAAAA");
		}
	}

    public static boolean addWine(String wine, String image2) {
        return false;
    }

    public static boolean wineExists(String wine) {
        return false;
    }

    public static String getWine(String wine) {
        return null;
    }
}
