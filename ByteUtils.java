import java.nio.ByteBuffer;

public class ByteUtils {
	private static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);

	public static byte[] longToByteArray(long value) {
		byte[] byteArray = new byte[8];
		for (int i = 0; i < 8; i++) {
			byteArray[7 - i] = (byte) (value >>> (i * 8));
		}
		return byteArray;
	}

	public static byte[] longToBytes(long x) {
		buffer.putLong(0, x);
		return buffer.array();
	}

/* 	public static long byteArrayToLong(byte[] byteArray) {

		long value = 0;
		for (int i = 0; i < byteArray.length; i++) {
			value = (value << 8) + (byteArray[i] & 0xFF);
		}
		return value;
	} */
}
