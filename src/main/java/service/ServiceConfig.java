package service;

import cli.StegAlgorithm;
import cli.CryptoAlgorithm;
import cli.CryptoMode;

import java.util.Optional;

/**
 * Representa la configuración de una operación lista para ser procesada
 * por la capa de servicio. Contiene los datos de los archivos ya cargados en
 * memoria (byte arrays) y las opciones de algoritmos. (no conoce las rutas de los archivos).
 */
public record ServiceConfig(
    byte[] carrierData,
    byte[] secretData, // Puede ser null para la operación de extracción
    String inputFileName, // Necesario para obtener la extensión
    StegAlgorithm stegAlgorithm,
    Optional<CryptoAlgorithm> cryptoAlgorithm,
    Optional<CryptoMode> cryptoMode,
    Optional<String> password
) {}
