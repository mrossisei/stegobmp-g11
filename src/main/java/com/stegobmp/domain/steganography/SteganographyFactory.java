package com.stegobmp.domain.steganography;

import com.stegobmp.domain.crypto.CryptoConfig;
import com.stegobmp.domain.crypto.CryptoHandler;

public class SteganographyFactory {

    public static SteganographyStrategy getStrategy(StegAlgorithm algorithm) {
        switch (algorithm) {
            case LSB1 -> {
                return new SteganographyStrategyLSB1();
            }
            case LSB4 -> {
                return new SteganographyStrategyLSB4();
            }
            case LSBI -> {
                return new SteganographyStrategyLSBI2();
            }
            default -> throw new IllegalArgumentException("Unsupported steganography algorithm: " + algorithm);
        }
    }

}
