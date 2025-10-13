package com.stegobmp.domain.crypto;

public record CryptoConfig(
        CryptoAlgorithm cryptoAlgorithm,
        CryptoMode cryptoMode,
        String password
) {
}
