package com.stegobmp.domain.crypto;

import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoHandler {

    private final CryptoConfig cryptoConfig;

    public CryptoHandler(CryptoConfig cryptoConfig) {
        this.cryptoConfig = cryptoConfig;
    }

    private String getAlgorithmName() {
        return cryptoConfig.cryptoAlgorithm().name().replace("DES3", "DESede").startsWith("AES") ? "AES" : "DESede";
    }

    private byte[] getKeyBytes(String algorithm, String password) {
        int keyLength;
        if (algorithm.equals("AES")) {
            // Determine key length from the mode
            keyLength = switch (cryptoConfig.cryptoAlgorithm()) {
                case AES128 -> 16;
                case AES192 -> 24;
                case AES256 -> 32;
                default -> throw new IllegalArgumentException("Unsupported AES mode");
            };
        } else if (algorithm.equals("DESede")) {
            keyLength = 24;
        } else {
            throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
        byte[] keyBytes = new byte[keyLength];
        byte[] passwordBytes = password.getBytes();
        System.arraycopy(passwordBytes, 0, keyBytes, 0, Math.min(passwordBytes.length, keyLength));
        return keyBytes;
    }

    public byte[] encrypt(byte[] data) {
        try {
            String algorithm = getAlgorithmName();
            String mode = cryptoConfig.cryptoMode().name();
            byte[] keyBytes = getKeyBytes(algorithm, cryptoConfig.password());
            String transformation = algorithm + "/" + mode + "/PKCS5Padding";

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, algorithm);
            Cipher cipher = Cipher.getInstance(transformation);

            if (mode.equals("ECB")) {
                cipher.init(Cipher.ENCRYPT_MODE, keySpec);
                return cipher.doFinal(data);
            } else {
                int ivLength = algorithm.startsWith("AES") ? 16 : 8; // AES block size is 16, DESede is 8
                byte[] iv = new byte[ivLength];
                new SecureRandom().nextBytes(iv);
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
                byte[] encrypted = cipher.doFinal(data);
                byte[] result = new byte[iv.length + encrypted.length];
                System.arraycopy(iv, 0, result, 0, iv.length);
                System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
                return result;
            }
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public byte[] decrypt(byte[] data) {
        try {
            String algorithm = getAlgorithmName();
            String mode = cryptoConfig.cryptoMode().name();
            byte[] keyBytes = getKeyBytes(algorithm, cryptoConfig.password());
            String transformation = algorithm + "/" + mode + "/PKCS5Padding";

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, algorithm);
            Cipher cipher = Cipher.getInstance(transformation);

            if (mode.equals("ECB")) {
                cipher.init(Cipher.DECRYPT_MODE, keySpec);
                return cipher.doFinal(data);
            } else {
                int ivLength = algorithm.startsWith("AES") ? 16 : 8; // AES block size is 16, DESede is 8
                byte[] iv = new byte[ivLength];
                System.arraycopy(data, 0, iv, 0, ivLength);
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
                return cipher.doFinal(data, ivLength, data.length - ivLength);
            }
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }


}
