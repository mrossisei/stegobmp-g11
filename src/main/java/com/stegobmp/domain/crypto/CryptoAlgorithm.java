package com.stegobmp.domain.crypto;

public enum CryptoAlgorithm {
    AES128,
    AES192,
    AES256,
    DES3;

    public static CryptoAlgorithm fromString(String text) {
        if ("3des".equalsIgnoreCase(text)) {
            return DES3;
        }
        try {
            return CryptoAlgorithm.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}