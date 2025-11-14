package com.stegobmp.cli;

import com.stegobmp.domain.crypto.CryptoAlgorithm;
import com.stegobmp.domain.crypto.CryptoMode;
import com.stegobmp.domain.steganography.StegAlgorithm;
import com.stegobmp.exception.StegoException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CommandLineParser {

    public static CliConfig parse(String[] args) throws StegoException {
        if (args.length == 0) {
            throw new StegoException("No se proporcionaron argumentos. Uso: stegobmp -embed | -extract ...");
        }

        Map<String, String> argMap = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    argMap.put(args[i], args[i + 1]);
                    i++;
                } else {
                    argMap.put(args[i], null);
                }
            }
        }

        OperationMode operationMode = parseOperationMode(argMap);

        String carrierFile = getRequiredArg(argMap, "-p");
        String outputFile = getRequiredArg(argMap, "-out");
        StegAlgorithm stegAlgorithm = StegAlgorithm.fromString(getRequiredArg(argMap, "-steg"));

        String inputFile = null;
        if (operationMode == OperationMode.EMBED) {
            inputFile = getRequiredArg(argMap, "-in");
        }

        Optional<String> password = Optional.ofNullable(argMap.get("-pass"));
        Optional<CryptoAlgorithm> cryptoAlgorithm = Optional.ofNullable(argMap.get("-a")).map(CryptoAlgorithm::fromString);
        Optional<CryptoMode> cryptoMode = Optional.ofNullable(argMap.get("-m")).map(CryptoMode::fromString);

        if ((cryptoAlgorithm.isPresent() || cryptoMode.isPresent()) && password.isEmpty()) {
            throw new StegoException("Se especificó un algoritmo/modo de cifrado pero falta el parámetro -pass <password>.");
        }

        if (password.isPresent()) {
            if (cryptoAlgorithm.isEmpty()) {
                cryptoAlgorithm = Optional.of(CryptoAlgorithm.AES128);
            }
            if (cryptoMode.isEmpty()) {
                cryptoMode = Optional.of(CryptoMode.CBC);
            }
        }

        return new CliConfig(
                operationMode,
                inputFile,
                carrierFile,
                outputFile,
                stegAlgorithm,
                cryptoAlgorithm,
                cryptoMode,
                password
        );
    }

    private static OperationMode parseOperationMode(Map<String, String> argMap) throws StegoException {
        boolean hasEmbed = argMap.containsKey("-embed");
        boolean hasExtract = argMap.containsKey("-extract");

        if (hasEmbed && hasExtract) {
            throw new StegoException("No se puede especificar -embed y -extract al mismo tiempo.");
        }
        if (!hasEmbed && !hasExtract) {
            throw new StegoException("Se debe especificar la operación: -embed o -extract.");
        }
        return hasEmbed ? OperationMode.EMBED : OperationMode.EXTRACT;
    }

    private static String getRequiredArg(Map<String, String> argMap, String key) throws StegoException {
        String value = argMap.get(key);
        if (value == null) {
            throw new StegoException("Falta el parámetro obligatorio: " + key);
        }
        return value;
    }
}