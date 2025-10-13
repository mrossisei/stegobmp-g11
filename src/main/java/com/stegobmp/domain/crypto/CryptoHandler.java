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

    public byte[] encrypt(byte[] data) {
        try {
            String algorithm = cryptoConfig.cryptoAlgorithm().name().replace("DES3", "DESede");
            String mode = cryptoConfig.cryptoMode().name();
            String transformation = algorithm + "/" + mode + "/PKCS5Padding";

            SecretKeySpec keySpec = new SecretKeySpec(cryptoConfig.password().getBytes(), algorithm);
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


}
