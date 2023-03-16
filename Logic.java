import java.io.IOException;
import java.util.Hashtable;

public class Logic {

    private static User currentUser = null;

    public User autheticate(String user , String password) throws IOException{
        return (currentUser = new User(user, password));
    }

    /*
     * add <wine> <image> - adiciona um novo vinho identificado por wine, associado à imagem 
     * image.    Caso  já  exista  um  vinho  com  o  mesmo  nome  deve  ser  devolvido  um  erro. 
     * Inicialmente o vinho não terá qualquer classificação e o número de unidades disponíveis 
     * será zero.
     */
    public static boolean add(String wine, String image) throws IOException{
        return Wine.addWine(wine, image);
    }

    /*
     * sell  <wine>  <value>  <quantity>  -  coloca  à  venda  o  número  indicado  por  quantity  de 
     * unidades do vinho wine pelo valor value. Caso o wine não exista, deve ser devolvido um 
     * erro.
     */
    public static boolean sell(String userId, String wine, int quantity) {
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
     */
    public static String view(String wine) {
		return (!Wine.wineExists(wine)) ? null : Wine.getWine(wine) + "\n" + Listings.getListing(wine);
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
    public static boolean buy(String seller, String wine, int quantity) throws IOException{
        return Listings.available(seller, wine, quantity) && User.buy(quantity, Listings.getPrice(seller, wine)) && Listings.removeListing(seller, wine, quantity);
    }

    /**
     * obtém o saldo atual da carteira do utilizador
     * @return
     */
    public static String wallet(){
        return String.valueOf(currentUser.getBalance());
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
        return MsmContainer.sendMessage(dest, message);
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
