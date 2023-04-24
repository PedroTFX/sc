import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.UUID;

/**
 * 1. Create next block
 * 2. Append NFTs and NFT purchases
 * 3. We have 5 of these (transaction number is 0)? Start over
 */

public class Blockchain {
	long blockNumber = 0;
	int transactionNumber = 0;
	PrivateKey privateKey = null;

	Blockchain(PrivateKey privateKey) throws Exception {
		this.privateKey = privateKey;

		if (!verify()) {
			throw new Exception("Blockchain corrupted! Aborting...");
		}

		// If empty block chain, create next block
		if (blockNumber == 0) {
			createNextBlock();
		}
	}

	private void createNextBlock() throws Exception {
		// Generate footer
		if(blockNumber > 0) {
			String blockSignature = generateBlockSignature();
			appendToBlock(blockSignature);
		}

		// Advance to next block
		createNextBlockFile();

		// Generate header of next block
		String header = generateBlockHeader();
		appendToBlock(header);

	}

	private boolean verify() {
		return true;
	}

	private void createNextBlockFile() {
		blockNumber++;
		File blockFile = new File(getCurrentBlockFilename());
		if (!blockFile.exists()) {
			blockFile.mkdir();
			System.out.println("Next block file created ✅");
		}
	}

	private String generateBlockHeader() {
		String hash = blockNumber == 0 ? "0000000000000000000000000000000000000000000000000000000000000000"
				: getBlockHash(blockNumber - 1);
		String block = String.format("%d", blockNumber);
		String transaction = String.format("%d", transactionNumber);
		transactionNumber = (transactionNumber + 1) % 5;
		String header = String.format("%s %s %s\n", hash, block, transaction);
		return header;
	}


	private String generateBlockSignature() throws Exception {
		// Colocar todos os bytes do block_xxx.blk no array
		Path path = Paths.get(getCurrentBlockFilename());
		byte[] bytes = Files.readAllBytes(path);
		byte[] signedTransaction = SecurityRSA.sign(bytes, privateKey);
		String base64SignedTransaction = Base64.getEncoder().encodeToString(signedTransaction);
		return base64SignedTransaction;
	}

	public void addNFT(String userName, String uuid, String wineName, int quantity, int price, byte[] signature) throws Exception {
		String base64Signature = Base64.getEncoder().encodeToString(signature);
		// <userId>:<uuid>:<wineName>:<quantity>:<price>:<base64Signature>
		String NFT = String.format("%s:%s:%s:%d:%d:%s", userName, uuid, wineName, quantity, price, base64Signature);
		appendToBlock(NFT);
	}

	public void buyNFT() {

	}

	private void appendToBlock(String str) throws Exception {
		BufferedWriter bw = new BufferedWriter(new FileWriter(getCurrentBlockFilename(), true));
		bw.append(str);
		bw.close();
	}

	private String getCurrentBlockFilename() {
		return String.format("./%s/block_%d", Constants.BLOCKCHAIN_FOLDER, blockNumber);
	}


	private String getBlockHash(long blockN) {
		return "";
	}
}
