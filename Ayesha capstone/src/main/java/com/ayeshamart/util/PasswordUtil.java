package com.ayeshamart.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Secure password hashing using PBKDF2WithHmacSHA256.
 * <p>
 * Stored format: {@code iterations:saltBase64:hashBase64}
 * e.g. {@code 120000:k9f2...:3qT7...}
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /** Generates a salted PBKDF2 hash for a plain-text password. */
    public static String hashPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password must not be null");
        }
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        byte[] derivedKey = derive(password, salt, ITERATIONS, KEY_LENGTH_BITS);
        return ITERATIONS + ":" + base64(salt) + ":" + base64(derivedKey);
    }

    /** Verifies a plain-text password against a stored PBKDF2 hash. */
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null || storedHash.trim().isEmpty()) {
            return false;
        }
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 3) {
                return false;
            }
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[2]);
            byte[] actualHash = derive(password, salt, iterations, KEY_LENGTH_BITS);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLengthBits);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 hashing failed", e);
        }
    }

    private static String base64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
