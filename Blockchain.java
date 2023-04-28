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
	long blockNumber = 0;
	int transactionNumber = 0;
	PrivateKey privateKey = null;

	Blockchain(PrivateKey privateKey) throws Exception {
		this.privateKey = privateKey;

		if (!verify()) {
			throw new Exception("Blockchain corrupted! Aborting...");
		}

		// If empty block chain, create first block
		if (blockNumber == 0) {
			createNextBlock();
		}
	}

	public void addNFT(String nft) throws Exception {
		appendToBlock(nft);
		incrementTransaction();
	}

	public void buyNFT(String nft) throws Exception {
		appendToBlock(nft);
		incrementTransaction();
	}

	private boolean verify() throws Exception {
		File[] allFiles = getAllBlocks(Constants.BLOCKCHAIN_FOLDER);
		if (allFiles.length == 0) {
			return true;
		}
		this.blockNumber = allFiles.length;

		for (int i = 1; i < allFiles.length; i++) {
			// check if previous block is valid
			String currentBlockContent = new String(Files.readAllBytes(allFiles[i].toPath()));
			String[] currentBlockLines = currentBlockContent.split("\n");
			String currentBlockHeader = currentBlockLines[0];
			String[] currentBlockHeaderParts = currentBlockHeader.split(" ");
			String previousBlockStoredHash = currentBlockHeaderParts[0];
			String previousBlockContent = new String(Files.readAllBytes(allFiles[i - 1].toPath()));
			String previousBlockCalculatedHash = Integrity.base64Hash(previousBlockContent);
			if (!previousBlockStoredHash.equals(previousBlockCalculatedHash)) {
				return false;
			} else {
				System.out.println("Block #" + i + " is valid");
			}

			// If we're on the last block, update transaction number
			boolean lastBlock = i == allFiles.length - 1;
			if (lastBlock) {
				transactionNumber = currentBlockLines.length - 1;
			}
		}

		return true;
	}

	private static File[] getAllBlocks(String blocksDirectory) {
		File folder = new File(blocksDirectory);
		return folder.listFiles();
	}

	private void createNextBlock() throws Exception {
		// Generate block header
		String nextBlockHeader = generateNextBlockHeader();

		// Create next block
		blockNumber++;
		createNextBlockFile();
		appendToBlock(nextBlockHeader);
		transactionNumber = 0;
	}

	private String generateNextBlockHeader() throws Exception {
		String hash = blockNumber == 0 ? "0000000000000000000000000000000000000000000000000000000000000000"
				: getCurrentBlockHash();
		return String.format("%s %d %d", hash, blockNumber + 1, 0);
	}

	private String getCurrentBlockHash() throws Exception {
		Path path = Paths.get(getCurrentBlockFilename());
		byte[] bytes = Files.readAllBytes(path);
		String blockContent = new String(bytes);
		return Integrity.base64Hash(blockContent);
	}

	private void incrementTransaction() throws Exception {
		transactionNumber = (transactionNumber + 1) % 6;
		if (transactionNumber == 5) {
			appendToBlock(generateCurrentBlockSignature());
			createNextBlock();
		}
	}

	private String generateCurrentBlockSignature() throws Exception {
		Path path = Paths.get(getCurrentBlockFilename());
		byte[] bytes = Files.readAllBytes(path);
		byte[] signedTransaction = SecurityRSA.sign(bytes, privateKey);
		return Base64.getEncoder().encodeToString(signedTransaction);
	}

	private void appendToBlock(String str) throws Exception {
		BufferedWriter bw = new BufferedWriter(new FileWriter(getCurrentBlockFilename(), true));
		bw.append(str);
		bw.newLine();
		bw.close();
	}

	private String getCurrentBlockFilename() {
		return String.format("./%s/block_%d.blk", Constants.BLOCKCHAIN_FOLDER, blockNumber);
	}

	private void createNextBlockFile() throws IOException {
		File blockFile = new File(getCurrentBlockFilename());
		blockFile.createNewFile();
	}
}
