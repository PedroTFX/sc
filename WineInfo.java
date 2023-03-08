import java.io.IOException;

public class WineInfo {

	public static void addWine(String wine, String imagem){
		String lineToWrite = buildInicialTxtLine(wine);
		Data.writeOnFile(lineToWrite, "wines.txt");
	}

	private static String buildInicialTxtLine(String wine){
		return wine + ";0;0";
	}

	public static void classifyWine(String wine, int review){
		String wineInfo = Data.readInfoFromFile(wine, "wines.txt");
		String reviews = wineInfo.split(";")[2];
		try {
			Data.updateLine(wine + ":" + wineInfo, wine + builTxtLine(wine, wineInfo.split(";"), reviews, 2), "wines.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String builTxtLine(String wine, String[] wineInfo, String infoToChange, int indexToChange){
		for (int i = 0; i < wineInfo.length; i++) {
			if(i == indexToChange){
				wineInfo[i] = infoToChange;
			}
		}

		String updatedLine = null;
		for (int i = 0; i < wineInfo.length; i++) {
			updatedLine += wineInfo + ";";
		}

		return wine + ":" + updatedLine.substring(0, updatedLine.length() - 1);
	}

	public static int getQuantity(String wine){
		String wineInfo = Data.readInfoFromFile(wine, "wines.txt");
		return Integer.parseInt(wineInfo.split(":")[1].split(";")[1]);
	}

	public static int getValue(String wine){
		String wineInfo = Data.readInfoFromFile(wine, "wines.txt");
		return Integer.parseInt(wineInfo.split(":")[0].split(";")[0]);
	}

	public static void setQuantity(String wine, int quantity){
		String wineInfo = Data.readInfoFromFile(wine, "wines.txt");
		String[] attributes = wineInfo.split(";");
		String lineToWrite = builTxtLine(wine, attributes, String.valueOf(quantity), 1);
		try {
			Data.updateLine(wine + ":" + wineInfo, wine + ":" + lineToWrite, "wines.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
