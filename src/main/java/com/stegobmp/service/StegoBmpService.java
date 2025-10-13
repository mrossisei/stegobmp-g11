package com.stegobmp.service;

import com.stegobmp.domain.bmp.BmpHandler;
import com.stegobmp.domain.bmp.BmpImage;
import com.stegobmp.domain.crypto.CryptoConfig;
import com.stegobmp.domain.crypto.CryptoHandler;
import com.stegobmp.domain.payload.ExtractedFile;
import com.stegobmp.domain.payload.PayloadHandler;
import com.stegobmp.domain.steganography.SteganographyFactory;
import com.stegobmp.domain.steganography.SteganographyStrategy;
import com.stegobmp.exception.StegoException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

/**
 * Orquestador principal de la aplicación.
 * Coordina las llamadas a los componentes del dominio (handlers, strategies, etc.).
 */
public class StegoBmpService {

    private final BmpHandler bmpHandler;
    private final PayloadHandler payloadHandler;
    private final ServiceConfig config;
    private final CryptoHandler cryptoHandler;
    private String lastExtractedFileExtension = "";
    public StegoBmpService(ServiceConfig serviceConfig) {
        this.bmpHandler = new BmpHandler();
        this.payloadHandler = new PayloadHandler();
        this.config = serviceConfig;
        this.cryptoHandler = config.cryptoConfig().map(CryptoHandler::new).orElse(null);

    }

    //Proceso de ocultar información.
    public byte[] embed() throws IOException {
        BmpImage carrierImage = bmpHandler.parseBmp(config.carrierData());

        byte[] payloadToEmbed = payloadHandler.preparePayload(config.secretData(), config.inputFileName(), cryptoHandler);

        SteganographyStrategy strategy = SteganographyFactory.getStrategy(config.stegAlgorithm(), config.cryptoConfig().orElse(null));

        if (strategy.getCapacity(carrierImage.pixelData()) < payloadToEmbed.length) {
            throw new StegoException("El archivo portador no tiene suficiente capacidad para ocultar los datos.");
        }

        byte[] newPixelData = strategy.embed(carrierImage.pixelData(), payloadToEmbed);
        BmpImage outputImage = new BmpImage(carrierImage.header(), newPixelData);
        return bmpHandler.writeBmpToBytes(outputImage);
    }

    public byte[] extract() throws IOException {
        BmpImage carrierImage = bmpHandler.parseBmp(config.carrierData());
        SteganographyStrategy strategy = SteganographyFactory.getStrategy(config.stegAlgorithm(), config.cryptoConfig().orElse(null));
        byte[] extractedPayload = strategy.extract(carrierImage.pixelData(), config.cryptoConfig().isEmpty());

        ExtractedFile extractedFile = payloadHandler.parsePayload(extractedPayload, cryptoHandler);

        ExtractedData extractedData = payloadHandler.extractFileExtension(extractedPayload);
        setLastExtractedFileExtension(extractedData.extension);
        return extractedData.payload;
    }
    public String getLastExtractedFileExtension() {
        return lastExtractedFileExtension;
    }
    public void setLastExtractedFileExtension(String ext) {
        this.lastExtractedFileExtension = ext;
    }

    public record ExtractedData(byte[] payload, String extension) {
    }
}