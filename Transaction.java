public class Transaction {
	private String wineName;
	private int quantity;
	private double value;
	private String buyer;
	private String seller;
	private byte[] signedTransaction;
	private byte[] transaction;

	public Transaction(String wineName, int quantity, double value, String buyer, String seller, byte[] signedTransaction, byte[] transaction) {
		this.wineName = wineName;
		this.value = value;
		this.value = value;
		this.buyer = buyer;
		this.seller = seller;
		this.signedTransaction = signedTransaction;
		this.transaction = transaction;
	}

}

