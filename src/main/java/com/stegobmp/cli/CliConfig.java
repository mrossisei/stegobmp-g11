package com.stegobmp.cli;

import com.stegobmp.domain.crypto.CryptoAlgorithm;
import com.stegobmp.domain.crypto.CryptoMode;
import com.stegobmp.domain.steganography.StegAlgorithm;

import java.util.Optional;

/**
 * Representa la configuración extraída directamente de los argumentos
 * de la línea de comandos. Contiene las rutas a los archivos como Strings
 * y las opciones seleccionadas por el usuario.
 * Es un objeto inmutable específico de la capa de presentación (CLI).
 */
public record CliConfig(
        OperationMode operationMode,
        String inputFile,
        String carrierFile,
        String outputFile,
        StegAlgorithm stegAlgorithm,
        Optional<CryptoAlgorithm> cryptoAlgorithm,
        Optional<CryptoMode> cryptoMode,
        Optional<String> password
) {}