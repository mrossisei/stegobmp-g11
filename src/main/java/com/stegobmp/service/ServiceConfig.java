package com.stegobmp.service;

import com.stegobmp.domain.steganography.StegAlgorithm;
import com.stegobmp.domain.crypto.CryptoConfig;

import java.util.Optional;

public record ServiceConfig(
    byte[] carrierData,
    byte[] secretData,
    String inputFileName,
    StegAlgorithm stegAlgorithm,
    Optional<CryptoConfig> cryptoConfig
) {}
