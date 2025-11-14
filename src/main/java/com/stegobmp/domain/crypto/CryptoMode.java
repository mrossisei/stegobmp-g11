package com.stegobmp.domain.crypto;

public enum CryptoMode {
    ECB,
    CFB,
    OFB,
    CBC;

    public static CryptoMode fromString(String text) {
        try {
            return CryptoMode.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
           return null;
        }
    }
}