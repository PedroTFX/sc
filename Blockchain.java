import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

/**
 * 1. Create next block
 * 2. Append NFTs and NFT purchases
 * 3. We have 5 of these (transaction number is 0)? Start over
 */
// TODO atualizar block number quando reinicio o servidor
// TODO RESOLVER EOF QUANDO CRIO NOVO BLOCO NA COMPRA DE UM NFT COMO E QUE O
// IDENTIFICAMOS
public class Blockchain {
	long blockNumber = 1;
	int transactionNumber = 0;
	PrivateKey privateKey = null;

	Blockchain(PrivateKey privateKey) throws Exception {
		this.privateKey = privateKey;

		if (!verify()) {
			throw new Exception("Blockchain corrupted! Aborting...");
		}

		// If empty block chain, create next block
		if (blockNumber == 1) {
			createFirstBlock();
		}
	}

	private void createFirstBlock() throws Exception {
		// Advance to next block
		createNextBlockFile();

		// Generate header of next block
		String header = generateBlockHeader();
		appendToBlock(header);
	}

	private void createNextBlock() throws Exception {
		// Generate footer
		if (blockNumber > 1) {
			String blockSignature = generateBlockSignature();
			appendToBlock(blockSignature);
		}

		// Advance to next block
		createNextBlockFile();

		// Generate header of next block
		String header = generateBlockHeader();
		appendToBlock(header);
	}

	public static File[] getAllBlocks(String blocksDirectory) {
		File folder = new File(blocksDirectory);
		return folder.listFiles();
	}

	private boolean verify() throws IOException, NoSuchAlgorithmException {
		File[] allFiles = getAllBlocks(Constants.BLOCKCHAIN_FOLDER);
		if (allFiles.length == 0) {
			return true;
		}
		this.blockNumber = allFiles.length + 1;
		/*
		 * for (int i = 1; i < allFiles.length; i++) {
		 * String hashCurrentBlock = getHashLine(allFiles[i]);
		 * String hashPreviousBlock = Integrity.base64Hash(allFiles[i-1].toString(),
		 * "SHA-256");
		 * if (!hashBlockCurrent.equals(hashBlockPrevious)) {
		 * return false;
		 * }
		 * }
		 */

		return true;
	}

	private void createNextBlockFile() throws IOException {
		File blockFile = new File(getCurrentBlockFilename());
		if (!blockFile.exists()) {
			blockFile.createNewFile();
			System.out.println("Next block file created ✅");
		}
	}

	private String generateBlockHeader() throws NoSuchAlgorithmException, IOException {
		String hash = blockNumber == 1 ? "0000000000000000000000000000000000000000000000000000000000000000"
				: "";//Integrity.calculateFileHash(String.format("./%s/block_%d.blk", Constants.BLOCKCHAIN_FOLDER, blockNumber - 1), "SHA-256");
		String block = String.format("%d", blockNumber);
		String transaction = String.format("%d", transactionNumber);
		String header = String.format("%s %s %s", hash, block, transaction);
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

	public void addNFT(String nft) throws Exception {
		appendToBlock(nft);
		transactionNumber = (transactionNumber + 1) % 6;
		if (transactionNumber == 5) {
			String blockSignature = generateBlockSignature();
			appendToBlock(blockSignature);
			String blockHash = Integrity.calculateFileHash(String.format("./%s/block_%d.blk", Constants.BLOCKCHAIN_FOLDER, blockNumber - 1), "SHA-256");
			blockNumber++;
			createNextBlockFile();
			appendToBlock(blockHash + " " + blockNumber);
			transactionNumber = 0;
		}
	}

	public void buyNFT(String userName, String uuid, String wineName, int quantity, int price, byte[] signature)
			throws Exception {
		String base64Signature = Base64.getEncoder().encodeToString(signature);
		// <userId>:<uuid>:<wineName>:<quantity>:<price>:<base64Signature>
		String NFT = String.format("%s:%s:%s:%d:%d:%s", userName, uuid, wineName, quantity, price, base64Signature);
		appendToBlock(NFT);
		transactionNumber = (transactionNumber + 1) % 5;

		if (transactionNumber == 5) {
			String blockSignature = generateBlockSignature();
			appendToBlock(blockSignature);
			String blockHash = Integrity.calculateFileHash(
					String.format("./%s/block_%d.blk", Constants.BLOCKCHAIN_FOLDER, blockNumber), "SHA-256");
			blockNumber++;
			createNextBlockFile();
			appendToBlock(blockHash + " " + blockNumber);
			transactionNumber = 0;
		}
	}

	private void appendToBlock(String str) throws Exception {
		BufferedWriter bw = new BufferedWriter(new FileWriter(getCurrentBlockFilename(), true));
		bw.append(str);
		bw.newLine();
		bw.close();
	}

	private String getCurrentBlockFilename() {
		if (blockNumber == 1) {
			return String.format("./%s/block_%d.blk", Constants.BLOCKCHAIN_FOLDER, blockNumber);
		}
		return String.format("./%s/block_%d.blk", Constants.BLOCKCHAIN_FOLDER, blockNumber - 1);
	}

	private String getHashLine(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String hashLine = br.readLine().split(" ")[0];
		br.close();
		return hashLine;
	}
}
