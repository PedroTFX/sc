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
        if(classification < 0 || classification > 5){
            return false;
        }
        return Wine.classify(wine, classification);
    }

	public static boolean sendMessage(String sender, String recipient, String message) throws IOException {
		return Data.writeOnFile(recipient + ":" + sender + ":" + Data.escape(message), Constants.MESSAGE_FILE);
	}

	public static Hashtable<String, ArrayList<String>> getMessages(String userId) throws IOException {
		return Data.readMessagesFromFile(userId);
	}
}
