package io.hemin.engine.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class HashUtil {

    public static String sha1(byte[] data) throws NoSuchAlgorithmException {
        final MessageDigest md = MessageDigest.getInstance("SHA-1");
        return byteArray2Hex(md.digest(data));
    }

    private static String byteArray2Hex(byte[] hash) {
        final Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

}
