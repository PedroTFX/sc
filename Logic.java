import java.io.IOException;

public class Logic {

    private static User currentUser = null;

/*     public User autheticate(String user , String password) throws IOException{
        return (currentUser = new User(user, password));
    } */

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
    public static boolean sellWine(String userId, String wine, int quantity) throws IOException {
        //checkif wine exists
        if(!Wine.wineExists(wine) || quantity < 0){
            return false;
        }
		return Listings.addListing(userId, wine, quantity);
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

    //TODO the wine list must have a way to find the wine by seller
    /**
     * buy <wine> <seller> <quantity> - compra quantity unidades do vinho wine ao utilizador
     * seller. O número de unidades deve ser removido da quantidade disponível e deve ser
     * transferido o valor correspondente à compra da conta do comprador para o vendedor.
     * Caso o vinho não exista, ou não existam unidades suficientes, ou o comprador não tenha
     * saldo suficiente, deverá ser devolvido e assinalado o erro correspondente.
     * @param seller
     * @param wine
     * @param quantity
     * @return
     * @throws IOException
     */
    public static boolean buyWine(String seller, String wine, int quantity) throws IOException{

        return Listings.available(seller, wine, quantity) && User.buy(quantity, Listings.getPrice(seller, wine)) && Listings.buyListing(seller, wine, quantity);
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
		//return String.valueOf(currentUser.getBalance());
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

    /**
     * Permite  enviar  uma  mensagem  privada  ao  utilizador  user  (por
     * exemplo, uma pergunta relativa a um vinho à venda). Caso o utilizador não exista, deve
     * ser devolvido um erro.
     * @param dest
     * @param message
     * @return
     */
    public static boolean talk(String dest, String message){
        //TODO seperate list for user search ???
        return MsmContainer.sendMessage(currentUser.getId(), dest, message);
    }

    /**
     * Permite ler as novas mensagens recebidas. Deve ser apresentada a identificação do
     * remetente e a respetiva mensagem. As mensagens são removidas da caixa de mensagens
     * do servidor depois de serem lidas.
     * @return
     */
    public static String[] read(){
        return MsmContainer.getMSM(currentUser);
    }
}
