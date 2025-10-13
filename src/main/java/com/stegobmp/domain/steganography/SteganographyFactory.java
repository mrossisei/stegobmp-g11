package com.stegobmp.domain.steganography;

import com.stegobmp.domain.crypto.CryptoConfig;
import com.stegobmp.domain.crypto.CryptoHandler;

public class SteganographyFactory {

    public static SteganographyStrategy getStrategy(StegAlgorithm algorithm, CryptoConfig cryptoConfig) {
        CryptoHandler cryptoHandler = null;
        if (cryptoConfig != null) {
            cryptoHandler = new CryptoHandler(cryptoConfig);
        }
        switch (algorithm) {
            case LSB1 -> {
                return (cryptoHandler != null) ? new SteganographyStrategyLSB1(cryptoHandler) : new SteganographyStrategyLSB1();
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
