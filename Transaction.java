public class Transaction {
	private String wineName;
	private int numberOfBottles;
	private double valuePerUnit;
	private String buyer;
	private String seller;

	public Transaction(String wineName, int numberOfBottles, double valuePerUnit, String buyer, String seller) {
		this.wineName = wineName;
		this.numberOfBottles = numberOfBottles;
		this.valuePerUnit = valuePerUnit;
		this.buyer = buyer;
		this.seller = seller;
	}

	// Getters and setters
}
