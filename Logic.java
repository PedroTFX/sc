import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

public class Logic {

    /*
     * add <wine> <image> - adiciona um novo vinho identificado por wine, associado à imagem
     * image.    Caso  já  exista  um  vinho  com  o  mesmo  nome  deve  ser  devolvido  um  erro.
     * Inicialmente o vinho não terá qualquer classificação e o número de unidades disponíveis
     * será zero.
     */
    public static boolean addWine(String wine, String user) throws IOException{
        return Wine.addWine(wine, user);
    }

    /*
     * sell  <wine>  <value>  <quantity>  -  coloca  à  venda  o  número  indicado  por  quantity  de
     * unidades do vinho wine pelo valor value. Caso o wine não exista, deve ser devolvido um
     * erro.
     */
    public static boolean sellWine(String userId, String wine, int quantity, int value) throws IOException {
        //checkif wine exists
        if(!Wine.wineExists(wine) || quantity < 0){
            return false;
        }
		return Listings.addListing(userId, wine, quantity, value);
	}

    /**
     * Obtém as informações associadas ao vinho identificado por wine,
     * nomeadamente  a  imagem  associada,  a  classificação  média  e,  caso  existam  unidades  do
     * vinho  disponíveis  para  venda,  a  indicação  do  utilizador  que  as  disponibiliza,  o  preço  e  a
     * quantidade disponível. Caso o vinho wine não exista, deve ser devolvido um erro.
     * @param wine
     * @return
     * @throws IOException
     */
    public static String viewWine(String wine) throws IOException {
		return (!Wine.wineExists(wine)) ? null : Wine.getWine(wine);
	}

    /**
     * obtém o saldo atual da carteira do utilizador
     * @return
     * @throws IOException
     */
    public static String wallet(String user) throws IOException{
		String info = Data.readUserInfoFromFile(user);
		if (info == null){
			return null;
		}
		String[] infoTokens = info.split(":");
		if (infoTokens.length == 3){
			return infoTokens[2];
		}
		return null;
    }

    /**
     * Atribui ao vinho wine uma classificação de 1 a 5, indicada por stars.
     * Caso o vinho wine não exista, deve ser devolvido um erro
     * @param wine
     * @param classification
     * @return
     * @throws IOException
     */
    public static boolean classify(String wine, int classification) throws IOException{
        if(classification < 1 || classification > 5){
            return false;
        }
        return Wine.classify(wine, classification);
    }

	public static boolean sendMessage(String sender, String recipient, String message) throws IOException {
		String userInfo = Data.readUserInfoFromFile(recipient);
		if (userInfo != null) {
			return Data.writeOnFile(recipient + ":" + sender + ":" + Data.escape(message), Constants.MESSAGE_FILE);
		}
		return false;
	}

	public static Hashtable<String, ArrayList<String>> getMessages(String userId) throws IOException {
		return Data.readMessagesFromFile(userId);
	}

	public static boolean buy(String buyer, String wine, String seller, int quantity) throws IOException{
		//String wine = request.wine;

		// Se este vinho não estiver à venda
		String sellInfo = Data.readSellInfo(wine, seller);
		if (sellInfo == null) {
/* 			response.type = Response.Type.ERROR;
			response.message = "Nao ha esse vinho a venda"; */
			return false;
		}

		// Extrair dados
		String[] sellInfoTokens = sellInfo.split(":");
		//String buyer = userId;
		//String seller = sellInfoTokens[1];
		int orderQuantity = /* request. */quantity;
		int stock = Integer.parseInt(sellInfoTokens[2]);
		int price = Integer.parseInt(sellInfoTokens[3]);
		int buyerBalance = Integer.parseInt(Data.readUserInfoFromFile(buyer).split(":")[2]);
		int sellerBalance = Integer.parseInt(Data.readUserInfoFromFile(seller).split(":")[2]);

		// Calcular stock atualizada
		int newStock = stock - orderQuantity;

		// Calcular saldo do vendedor atualizado
		int newSellerBalance = sellerBalance + orderQuantity * price;

		// Calcular saldo do comprador atualizado
		int newBuyerBalance = buyerBalance - orderQuantity * price;

		// Se não houverem vinhos suficientes
		if (newStock < 0) {
/* 			response.type = Response.Type.ERROR;
			response.message = "Nao ha quantidade suficiente"; */
			return false;
		}

		// Se o compardor não tiver saldo suficiente
		if (newBuyerBalance < 0) {
/* 			response.type = Response.Type.ERROR;
			response.message = "Nao ha saldo suficiente"; */
			return false;
		}

		// Autalizar stock de vinho na dase de dados
		Data.updateWineStock(wine, buyer, newStock, seller);

		// Atualizar saldo do vendedor na dase de dados
		Data.updateUserBalance(seller, newSellerBalance);

		// Atualizar saldo do comprador na dase de dados
		Data.updateUserBalance(buyer, newBuyerBalance);
		return true;
	}
}
