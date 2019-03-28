package hemin.engine.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class HashUtil {

    /**
     * Calculates the SHA1 hash for given bytes.
     *
     * @param data The bytes to calculate the hash for.
     * @return The SHA1 hash.
     * @throws NoSuchAlgorithmException If no algorithm for the hash function is registered.
     */
    public static String sha1(byte[] data) throws NoSuchAlgorithmException {
        final MessageDigest md = MessageDigest.getInstance("SHA-1");
        return byteArray2Hex(md.digest(data));
    }

    /**
     * Calculates the Hex representation for given bytes.
     *
     * @param bytes The bytes to represent in Hex.
     * @return The Hex values of given bytes as String.
     */
    private static String byteArray2Hex(byte[] bytes) {
        final Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

}
