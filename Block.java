/* import java.util.List;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

public class Block {
	private List<Transaction> transactions;
	private long timeStamp;
	private String previousHash;
	private String hash;

	public Block(List<Transaction> transactions, String previousHash) {
		this.transactions = transactions;
		this.previousHash = previousHash;
		this.timeStamp = Instant.now().getEpochSecond();
		this.hash = calculateHash();
	}

	public String calculateHash() {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			String input = previousHash + Long.toString(timeStamp) + transactions.toString();
			byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			StringBuilder hashString = new StringBuilder();
			for (byte hashByte : hashBytes) {
				hashString.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
			}
			return hashString.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	// Getters and setters
}
 */
