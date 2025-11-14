package com.stegobmp.cli;

import com.stegobmp.domain.crypto.CryptoAlgorithm;
import com.stegobmp.domain.crypto.CryptoMode;
import com.stegobmp.domain.steganography.StegAlgorithm;

import java.util.Optional;

public record StegoConfig(
        OperationMode operationMode,
        String inputFile,
        String carrierFile,
        String outputFile,
        StegAlgorithm stegAlgorithm,
        Optional<CryptoAlgorithm> cryptoAlgorithm,
        Optional<CryptoMode> cryptoMode,
        Optional<String> password
) {}
