import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class SecurityRSA {
	public static PrivateKey getPrivateKey(String keyStoreName, String keystorePassword, String alias)
			throws Exception {
		// Load keystore
		KeyStore keyStoreFile = KeyStore.getInstance("PKCS12");
		FileInputStream fis = new FileInputStream(keyStoreName);
		keyStoreFile.load(fis, keystorePassword.toCharArray());
		fis.close();

		// Get key from keystore
		Key key = keyStoreFile.getKey(alias, keystorePassword.toCharArray());

		// Check if it's a private key
		if (!(key instanceof PrivateKey)) {
			throw new Exception("The key is not a private key");
		}

		return (PrivateKey) key;
	}

	public static PublicKey getPublicKey(String keyStoreName, String keystorePassword, String alias) throws Exception {
		// Load keystore
		KeyStore keyStoreFile = KeyStore.getInstance("PKCS12");
		FileInputStream fis = new FileInputStream(keyStoreName);
		keyStoreFile.load(fis, keystorePassword.toCharArray());
		fis.close();

		// Load the public key
		Certificate cert = keyStoreFile.getCertificate(alias);
		Key key = cert.getPublicKey();

		// Check if it's a public key
		if (!(key instanceof PublicKey)) {
			throw new Exception("The key is not an RSA public key or is invalid.");
		}

		return (PublicKey) key;
	}

	public static byte[] encryptLong(long value, Key key) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(ByteUtils.longToBytes(value));
	}

	public static byte[] decryptLong(long value, PrivateKey key) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(ByteUtils.longToBytes(value));
	}

	public static String encryptAES128(String line, String passwordCifra) throws RuntimeException {
		try {
			//SecretKeySpec secretKey = new SecretKeySpec("yomamakeydeusman".getBytes(), "AES");
			SecretKeySpec secretKey = new SecretKeySpec(passwordCifra.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			byte[] encryptedBytes = cipher.doFinal(line.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(encryptedBytes);
		} catch (Exception e) {
			throw new RuntimeException("Error while encrypting line", e);
		}
	}

	public static String decryptAES128(String encryptedLine, String passwordCifra) throws RuntimeException {
		try {
			SecretKeySpec secretKey = new SecretKeySpec(passwordCifra.getBytes(), "AES");
			//SecretKeySpec secretKey = new SecretKeySpec("yomamakeydeusman".getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] decodedBytes = Base64.getDecoder().decode(encryptedLine);
			byte[] decryptedBytes = cipher.doFinal(decodedBytes);
			//return encryptedLine;
			return new String(decryptedBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new RuntimeException("Error while decrypting line", e);
		}
	}

	public static PublicKey decodePublicKey(String publicKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		// Base64 decoding to byte array
		byte[] publicKeyByteServer = Base64.getDecoder().decode(publicKey);
		// generate the publicKey
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKeyServer = (PublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyByteServer));
		return publicKeyServer;
	}

	public static String encodePublicKey(PublicKey publicKey) {
		return Base64.getEncoder().encodeToString(publicKey.getEncoded());
	}

	public static byte[] sign(byte[] toSign, PrivateKey key) throws Exception {
		Signature signature = Signature.getInstance("SHA512withRSA");
		signature.initSign(key/* , new SecureRandom() */);
		signature.update(toSign);
		return signature.sign();
	}

	public static boolean isSignedNonce(byte[] toSign, byte[] maybeSigned, PublicKey key) throws Exception {
		Signature signature = Signature.getInstance("SHA512withRSA");
		signature.initVerify(key);
		signature.update(toSign);
		return signature.verify(maybeSigned);
	}

	public static String encryptMessage(PublicKey publicKey, String encryptedMessage) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] encryptedMessageBytes = cipher.doFinal(encryptedMessage.getBytes(StandardCharsets.UTF_8));
		return new String(encryptedMessageBytes, StandardCharsets.UTF_8);
	}
}
