package com.ayeshamart.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

/**
 * Hashes and verifies passwords using PBKDF2 with HMAC-SHA256 and a per-user salt.
 * Stored format: "base64Salt$base64Hash" - the plaintext password is never stored.
 */
public final class PasswordUtil {

    private static final int ITERATIONS = 100_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_BYTES = 16;
    private static final String SEPARATOR = "$";
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /**
     * Hashes a plaintext password with a fresh random salt.
     */
    public static String hashPassword(String plainPassword) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(plainPassword.toCharArray(), salt);
        return Base64.getEncoder().encodeToString(salt) + SEPARATOR
                + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verifies a plaintext password against a stored "salt$hash" value.
     */
    public static boolean verifyPassword(String plainPassword, String stored) {
        if (stored == null || plainPassword == null) {
            return false;
        }
        int separatorIndex = stored.indexOf(SEPARATOR);
        if (separatorIndex <= 0) {
            return false;
        }
        try {
            byte[] salt = Base64.getDecoder().decode(stored.substring(0, separatorIndex));
            byte[] expectedHash = Base64.getDecoder().decode(stored.substring(separatorIndex + 1));
            byte[] actualHash = pbkdf2(plainPassword.toCharArray(), salt);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("PBKDF2WithHmacSHA256 algorithm not available", e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException("Invalid key specification for PBKDF2", e);
        }
    }
}