package com.stegobmp.cli;

import com.stegobmp.domain.crypto.CryptoConfig;
import com.stegobmp.exception.StegoException;
import com.stegobmp.service.StegoBmpService;
import com.stegobmp.service.ServiceConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class StegoBmp {

    public static void main(String[] args) throws IOException, StegoException {

        CliConfig cliConfig;
        try {
            cliConfig = CommandLineParser.parse(args);
        } catch (IllegalArgumentException | StegoException e) {
            System.err.println("Error: " + e.getMessage());
            return;
        }

        byte[] carrierData = readFileBytes(cliConfig.carrierFile());
        byte[] secretData = null;
        if (cliConfig.operationMode() == OperationMode.EMBED) {
            secretData = readFileBytes(cliConfig.inputFile());
        }

        Optional<CryptoConfig> cryptoConfig = Optional.empty();
        if (cliConfig.cryptoAlgorithm().isPresent()) {
            cryptoConfig = Optional.of(new CryptoConfig(
                    cliConfig.cryptoAlgorithm().get(),
                    cliConfig.cryptoMode().get(),
                    cliConfig.password().get()
            ));
        }

        ServiceConfig serviceConfig = new ServiceConfig(
                carrierData,
                secretData,
                cliConfig.inputFile(),
                cliConfig.stegAlgorithm(),
                cryptoConfig
        );

        StegoBmpService service = new StegoBmpService(serviceConfig);

        if (cliConfig.operationMode() == OperationMode.EMBED) {
            byte[] outputFileBytes = service.embed();
            writeFileBytes(cliConfig.outputFile(), outputFileBytes);
        } else {
            byte[] extractedFileBytes = service.extract();
            String outputFileName = cliConfig.outputFile();
            String extractedExt = service.getLastExtractedFileExtension();
            if (extractedExt != null && !extractedExt.isEmpty()) {
                if (!extractedExt.startsWith(".")) {
                    extractedExt = "." + extractedExt;
                }
                outputFileName += extractedExt;
            }
            writeFileBytes(outputFileName, extractedFileBytes);
        }


    }


    private static byte[] readFileBytes(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }

    private static void writeFileBytes(String path, byte[] data) throws IOException {
        Files.write(Paths.get(path), data);
    }
}


