package com.stegobmp.domain.crypto;

import java.security.spec.KeySpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class CryptoHandler {

    private final CryptoConfig cryptoConfig;

    private static final byte[] FIXED_SALT = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static final int PBKDF2_ITER = 10000;

        private record DerivedCryptoMaterial(SecretKey key, IvParameterSpec ivSpec) {
    }

    public CryptoHandler(CryptoConfig cryptoConfig) {
        this.cryptoConfig = cryptoConfig;
    }


    private String getAlgorithmName() {
        String name = cryptoConfig.cryptoAlgorithm().name();
        if (name.startsWith("AES")) {
            return "AES";
        }
        return "DESede";
    }


    private DerivedCryptoMaterial deriveKeyAndIv() throws Exception {
        String jceAlgorithmName = getAlgorithmName();
        String password = cryptoConfig.password();

        int keyLengthBytes = getKeyLengthBytes();

        int ivLengthBytes = jceAlgorithmName.equals("AES") ? 16 : 8;

        int keyLengthBits = keyLengthBytes * 8;
        int ivLengthBits = ivLengthBytes * 8;
        int totalBits = keyLengthBits + ivLengthBits;

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), FIXED_SALT, PBKDF2_ITER, totalBits);
        SecretKey tmp = factory.generateSecret(spec);
        byte[] derivedMaterial = tmp.getEncoded();

        byte[] keyBytes = Arrays.copyOfRange(derivedMaterial, 0, keyLengthBytes);
        byte[] ivBytes = Arrays.copyOfRange(derivedMaterial, keyLengthBytes, derivedMaterial.length);

        SecretKey secretKey = new SecretKeySpec(keyBytes, jceAlgorithmName);
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

        return new DerivedCryptoMaterial(secretKey, ivSpec);
    }

    private int getKeyLengthBytes() {
        return switch (cryptoConfig.cryptoAlgorithm()) {
            case AES128 -> 16;
            case AES256 -> 32;
            default -> 24;
        };
    }

    private String getTransformation() {
        String algorithm = getAlgorithmName();
        CryptoMode mode = cryptoConfig.cryptoMode();
        String padding = "PKCS5Padding";
        String modeName = mode.name();
        if (mode == CryptoMode.CFB || mode == CryptoMode.OFB) {
            padding = "NoPadding";
        }
        if (mode == CryptoMode.CFB) {
            modeName = "CFB8";
        }
        return algorithm + "/" + modeName + "/" + padding;
    }

    public byte[] encrypt(byte[] data) {
        try {
            DerivedCryptoMaterial material = deriveKeyAndIv();

            String transformation = getTransformation();
            Cipher cipher = Cipher.getInstance(transformation);

            if (cryptoConfig.cryptoMode() == CryptoMode.ECB) {
                cipher.init(Cipher.ENCRYPT_MODE, material.key);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, material.key, material.ivSpec);
            }

            return cipher.doFinal(data);

        } catch (Exception e) {
            throw new RuntimeException("Fallo la encriptación", e);
        }
    }


    public byte[] decrypt(byte[] data) {
        try {
            DerivedCryptoMaterial material = deriveKeyAndIv();

            String transformation = getTransformation();
            Cipher cipher = Cipher.getInstance(transformation);

            if (cryptoConfig.cryptoMode() == CryptoMode.ECB) {
                cipher.init(Cipher.DECRYPT_MODE, material.key);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, material.key, material.ivSpec);
            }

            return cipher.doFinal(data);

        } catch (Exception e) {
            throw new RuntimeException("Fallo la desencriptación", e);
        }
    }
}