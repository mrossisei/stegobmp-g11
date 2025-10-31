package com.stegobmp.domain.steganography;

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
                return new SteganographyStrategyLSBI();
            }
            default -> throw new IllegalArgumentException("Unsupported steganography algorithm: " + algorithm);
        }
    }

}
