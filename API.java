import java.security.PublicKey;
import java.util.ArrayList;

public class API {
	private DB db = null;

	public API(String cipher) {
		// this.cipher = cipher;
		db = new DB(cipher);
	}

	// USER
	public User addUser(User user) {
		String publicKeyString = SecurityRSA.encodePublicKey(user.key);
		try {
			String row = String.format("%s:%s:%d", user.name, publicKeyString, Constants.STARTING_BALANCE);
			db.user.add(row);
		} catch (Exception e) {
			return null;
		}
		return user;
	}

	public User getUser(String id) throws Exception {
		String row = db.user.get(id);
		if (row == null) {
			return null;
		}
		String[] tokens = row.split(":");
		String userID = tokens[0];
		PublicKey key = SecurityRSA.decodePublicKey(tokens[1]);
		int balance = Integer.parseInt(tokens[2]);
		return new User(userID, key, balance);
	}

	public User updateUser(User user) {

		return null;
	}

	public boolean deleteUser(String id) {
		return true;
	}

	public Wine addWine(Wine wine) throws Exception {
		boolean wineCreated = db.wine.get(wine.name) != null;
		if(wineCreated){
			return null;
		}

		// <wineName>:<evaluations>:<wineImageExtension>
		String row = String.format("%s::%s", wine.name, wine.extension);
		db.wine.add(row);

		ImageUtils.base64ToFile(Constants.SERVER_IMAGES_FOLDER, wine.name, wine.base64Image, wine.extension);

		return wine;
	}

	// Wine Listings
	public boolean listWine(String userName, String wineName, int quantity, int price) throws Exception {
		// Verificar se este vinho existe
		boolean wineNotExists = db.wine.get(wineName) == null;
		if (wineNotExists) {
			return false;
		}

		// Verificar se este user tem este vinho à venda
		boolean isSelling = db.listing.get(wineName, userName) != null;
		if (isSelling) {
			// Update
			String id = String.format("%s:%s", wineName, userName);
			String row = String.format("%s:%s:%d:%s", wineName, userName, quantity, price);
			db.listing.update(id, row);
		} else {
			// Create
			String row = String.format("%s:%s:%d:%s", wineName, userName, quantity, price);
			db.listing.add(row);
		}
		return true;
	}

	public ViewWine getWine(String id) throws Exception {
		// Check if wine exists
		String row = db.wine.get(id);
		if (row == null) {
			return null;
		}

		// Get Wine
		String[] tokens = row.split(":");
		String extension = null;
		if (tokens.length == 2) {
			extension = tokens[1];
		} else {
			extension = tokens[2];
		}

		String wineName = tokens[0];
		ArrayList<Integer> evals = new ArrayList<Integer>();
		if(tokens.length == 3) {
			String evalsString = tokens[1];
			if (!evalsString.equals("")){
				String[] evalsArray = evalsString.split(",");
				for (String eval : evalsArray) {
					evals.add(Integer.parseInt(eval));
				}
			}
		}

		String base64Image = ImageUtils.fileToBase64(Constants.SERVER_IMAGES_FOLDER, wineName + "." + extension);
		Wine wine = new Wine(wineName, base64Image, extension, evals);

		// Get listings
		ArrayList<Listing> listings = new ArrayList<Listing>();
		ArrayList<String> lines = db.listing.getAll(wineName);
		for (String line : lines) {
			String[] wineTokens = line.split(":");
			String name = wineTokens[0];
			String seller = wineTokens[1];
			String quantity = wineTokens[2];
			String price = wineTokens[3];
			listings.add(new Listing(seller, name, Integer.parseInt(price), Integer.parseInt(quantity)));
		}

		return new ViewWine(wine, listings);
	}

	public Response classifyWine(String name, int stars) throws Exception {
		String line = db.wine.get(name);
		if (line == null) {
			return new Response(Response.Type.ERROR, new Response.Error("Vinho nao existe"));
		}
		String[] linetokens = line.split(":");
		if (linetokens[1].equals("")) {
			linetokens[1] = String.valueOf(stars);
			String updatedLine = String.join(":", linetokens);
			db.wine.update(name, updatedLine);
		} else if (!linetokens[1].equals("")) {
			linetokens[1] += "," + String.valueOf(stars);
			String updatedLine = String.join(":", linetokens);
			db.wine.update(name, updatedLine);
		}

		return new Response(Response.Type.OK, new Response.OK("Vinho classificado com sucesso"));
	}

	public Response buyWine(String name, int quantity, String seller, String buyer) throws Exception {
		String[] buyerInfo = db.user.get(buyer).split(":");

		// ver se o vinho existe
		String wineInfoRow = db.wine.get(name);
		if (wineInfoRow == null) {
			return new Response(Response.Type.ERROR, new Response.Error("O vinho nao existe"));
		}

		//ver se esta a venda
		String listInfo = db.listing.get(name, seller);
		if (listInfo == null) {
			return new Response(Response.Type.ERROR, new Response.Error("Não há nenhuma proposta de venda de " + seller));
		}

		//informacoes da venda
		String sellerInfo[] = db.user.get(seller).split(":");


		//String[] wineInfoRowTokens = wineInfoRow.split(":");

		String[] listInfoTokens = listInfo.split(":");

		int price = Integer.parseInt(listInfoTokens[3]);

		// ver se o comprador tem dinheiro
		int neededBalance = quantity * price;

		if (quantity > Integer.parseInt(listInfoTokens[2])) {
			return new Response(Response.Type.ERROR, new Response.Error("Nao ha unidades suficientes para realizar a compra"));
		}

		if(neededBalance > Integer.parseInt(buyerInfo[2])){
			return new Response(Response.Type.ERROR, new Response.Error("Nao ha saldo suficiente para realizar a compra"));
		}

		//subtrair o numero de unidades a venda na venda
		listInfoTokens[2] = String.valueOf(Integer.parseInt(listInfoTokens[2]) - quantity) ;

		// reduzir saldo comprador e aumentar vendedor
		buyerInfo[2] = String.valueOf(Integer.parseInt(buyerInfo[2]) - neededBalance);
		sellerInfo[2] = String.valueOf(Integer.parseInt(sellerInfo[2]) + neededBalance);

		db.listing.update(name, String.join(":", listInfoTokens));

		db.user.update(seller, String.join(":", sellerInfo));
		db.user.update(buyer, String.join(":", buyerInfo));
		return new Response(Response.Type.OK, new Response.OK("unidades compradas com sucesso"));
	}

	static String escape(String message) {
		return message.replace(":", "\\:");
	}

	static String unescape(String message) {
		return message.replace("\\:", ":");
	}

	public Response talk(String recipient, String message, String sender) throws Exception {

		String recipientInfo = db.user.get(recipient);

		if (recipientInfo == null) {
			return new Response(Response.Type.ERROR, new Response.Error("Recetor nao existe"));
		}

		String newRow = String.format("%s:%s:%s", recipient, sender, message);

		db.message.add(newRow);

		return new Response(Response.Type.OK, new Response.OK("Mensagem enviada com sucesso"));
	}
}
