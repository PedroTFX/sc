import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;

public class API {
	// private DB db = null;
	private Blockchain bc = null;

	public API(String cipher, PrivateKey privateKey) throws Exception {
		// db = new DB(cipher);
		try {
			bc = new Blockchain(privateKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// USER
	public User addUser(User user) {
		String publicKeyString = SecurityRSA.encodePublicKey(user.key);
		try {
			String row = String.format("%s:%s:%d", user.name, publicKeyString, Constants.STARTING_BALANCE);
			// db.user.add(row);
		} catch (Exception e) {
			return null;
		}
		return user;
	}

	public User getUser(String id) throws Exception {
		// String row = db.user.get(id);
		/*
		 * if (row == null) {
		 * return null;
		 * }
		 */
		String[] tokens = null;// row.split(":");
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
		boolean wineCreated = true;// db.wine.get(wine.name) != null;
		if (wineCreated) {
			return null;
		}

		// <wineName>:<evaluations>:<wineImageExtension>
		String row = String.format("%s::%s", wine.name, wine.extension);
		// db.wine.add(row);

		ImageUtils.base64ToFile(Constants.SERVER_IMAGES_FOLDER, wine.name, wine.base64Image, wine.extension);

		return wine;
	}

	// Wine Listings
	public boolean listWine(String username, WineSale listWine, String base64Signature) throws Exception {
		String wineName = listWine.name;
		String uuid = listWine.uuid;
		int price = listWine.price;
		int quantity = listWine.quantity;

		// Verificar se este vinho existe
		// boolean wineNotExists = db.wine.get(wineName) == null;
		/*
		 * if (wineNotExists) {
		 * return false;
		 * }
		 */

		// Verificar se este user tem este vinho à venda
		boolean isAlreadySelling = false;// db.listing.get(wineName, username) != null;
		if (isAlreadySelling) {
			// Update
			String id = String.format("%s:%s", wineName, username);
			String row = String.format("%s:%s:%d:%s", wineName, username, price, price);
			// db.listing.update(id, row);
		} else {
			// Create (wine_listing.txt)
			String row = String.format("%s:%s:%d:%s", wineName, username, price, price);
			// db.listing.add(row);

		}

		// Add transaction to Blockchain:
		// <sell/buy>:<uuid>:<username>:<wineName>:<quantity>:<price>:<base64Signature>
		String nft = String.format("sell:%s:%s:%s:%d:%d:%s", uuid, username, wineName, quantity, price,
				base64Signature);
		bc.addNFT(nft);
		return true;
	}

	public ViewWine getWine(String id) throws Exception {
		// Check if wine exists
		String row = null;// db.wine.get(id);
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
		if (tokens.length == 3) {
			String evalsString = tokens[1];
			if (!evalsString.equals("")) {
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
		ArrayList<String> lines = null; // db.listing.getAll(wineName);
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
		String line = null;// db.wine.get(name);
		if (line == null) {
			return new Response(Response.Type.ERROR, new Response.Error("Vinho nao existe"));
		}
		String[] linetokens = line.split(":");
		if (linetokens[1].equals("")) {
			linetokens[1] = String.valueOf(stars);
			String updatedLine = String.join(":", linetokens);
			// db.wine.update(name, updatedLine);
		} else if (!linetokens[1].equals("")) {
			linetokens[1] += "," + String.valueOf(stars);
			String updatedLine = String.join(":", linetokens);
			// db.wine.update(name, updatedLine);
		}

		return new Response(Response.Type.OK, new Response.OK("Vinho classificado com sucesso"));
	}

	public Response buyWine(String username, WinePurchase winePurchase, String base64Signature) throws Exception {

		String buyer = username;
		String name = winePurchase.name;
		String seller = winePurchase.seller;
		int quantity = winePurchase.quantity;
		String uuid = winePurchase.uuid;
		String[] buyerInfo = null;// db.user.get(buyer).split(":");

		// ver se o vinho existe
		String wineInfoRow = null;// db.wine.get(name);
		if (wineInfoRow == null) {
			return new Response(Response.Type.ERROR, new Response.Error("O vinho nao existe"));
		}

		// ver se esta a venda
		String listInfo = null; // db.listing.get(name, seller);
		if (listInfo == null) {
			return new Response(Response.Type.ERROR,
					new Response.Error("Não há nenhuma proposta de venda de " + seller));
		}

		// informacoes da venda
		String sellerInfo[] = null; // db.user.get(seller).split(":");

		// String[] wineInfoRowTokens = wineInfoRow.split(":");

		String[] listInfoTokens = listInfo.split(":");

		int price = Integer.parseInt(listInfoTokens[3]);

		// ver se o comprador tem dinheiro
		int neededBalance = quantity * price;

		if (quantity > Integer.parseInt(listInfoTokens[2])) {
			return new Response(Response.Type.ERROR,
					new Response.Error("Nao ha unidades suficientes para realizar a compra"));
		}

		if (neededBalance > Integer.parseInt(buyerInfo[2])) {
			return new Response(Response.Type.ERROR,
					new Response.Error("Nao ha saldo suficiente para realizar a compra"));
		}

		// subtrair o numero de unidades a venda na venda
		listInfoTokens[2] = String.valueOf(Integer.parseInt(listInfoTokens[2]) - quantity);

		// reduzir saldo comprador e aumentar vendedor
		buyerInfo[2] = String.valueOf(Integer.parseInt(buyerInfo[2]) - neededBalance);
		sellerInfo[2] = String.valueOf(Integer.parseInt(sellerInfo[2]) + neededBalance);

		// db.listing.update(name, String.join(":", listInfoTokens));
		// db.user.update(seller, String.join(":", sellerInfo));
		// db.user.update(buyer, String.join(":", buyerInfo));

		// bc.buyNFT(name, uuid, name, quantity, price, signature);

		// Add transaction to Blockchain:
		// <sell/buy>:<uuid>:<username>:<wineName>:<quantity>:<price>:<base64Signature>
		String nft = String.format("buy:%s:%s:%s:%d:%s", uuid, name, seller, quantity, base64Signature);
		bc.addNFT(nft);

		return new Response(Response.Type.OK, new Response.OK("unidades compradas com sucesso"));
	}

	static String escape(String message) {
		return message.replace(":", "\\:");
	}

	static String unescape(String message) {
		return message.replace("\\:", ":");
	}

	public Response talk(String recipient, String message, String sender, byte[] secretKey) throws Exception {

		String secretKeyString = Base64.getEncoder().encodeToString(secretKey);

		String recipientInfo = null;// db.user.get(recipient);

		if (recipientInfo == null) {
			return new Response(Response.Type.ERROR, new Response.Error("Recetor nao existe"));
		}

		String newRow = String.format("%s:%s:%s:%s", recipient, sender, message, secretKeyString);

		// db.message.add(newRow);

		return new Response(Response.Type.OK, new Response.OK("Mensagem enviada com sucesso"));
	}

	public Response read(String recipient) throws Exception {
		ArrayList<String> messages = null; // db.message.getAll(recipient);

		return new Response(Response.Type.READ, new Response.ReadMessages(messages));
	}
}
